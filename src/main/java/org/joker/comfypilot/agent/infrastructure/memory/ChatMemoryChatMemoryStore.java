package org.joker.comfypilot.agent.infrastructure.memory;

import dev.langchain4j.data.message.*;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ChatMemoryChatMemoryStore {

    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    private final Map<String, List<ChatMessage>> messagesByMemoryId = new ConcurrentHashMap<>(1024);

    public List<ChatMessage> getMessages(String wsSessionId) {
        return messagesByMemoryId.get(wsSessionId);
    }

    public boolean addMessage(String wsSessionId, ChatMessage chatMessage) {
        AtomicBoolean added = new AtomicBoolean(false);
        messagesByMemoryId.compute(wsSessionId, (ignored, oldMessages) -> {
            if (oldMessages != null) {
                if (!oldMessages.isEmpty() && (oldMessages.getLast() instanceof UserMessage) && !(chatMessage instanceof AiMessage)) {
                    oldMessages.add(AiMessage.from("用户中断对话"));
                }
                // 没执行进来说明会话消息未初始化或会话已经关闭了
                if (chatMessage instanceof SystemMessage) {
                    if (!oldMessages.isEmpty() && (oldMessages.getFirst() instanceof SystemMessage)) {
                        oldMessages.removeFirst();
                    }
                    oldMessages.addFirst(chatMessage);
                    added.set(true);
                } else {
                    oldMessages.add(chatMessage);
                    added.set(true);
                }
            }
            return oldMessages;
        });
        return added.get();
    }

    public boolean updateMessages(String wsSessionId, List<ChatMessage> messages) {
        // 规范化消息列表，确保符合LLM模型要求
        List<ChatMessage> normalizeMessages = normalizeMessages(messages);

        AtomicBoolean closed = new AtomicBoolean(false);

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            if (chatMessage instanceof UserMessage && (i == messages.size() - 1 || !(messages.get(i + 1) instanceof AiMessage))) {
                messages.add(i + 1, AiMessage.from("用户中断对话"));
            }
        }

        messagesByMemoryId.compute(wsSessionId, (ignored, oldMessages) -> {
            oldMessages = new CopyOnWriteArrayList<>(normalizeMessages);
            webSocketSessionManager.addRemovedCallback(wsSessionId, () -> {
                messagesByMemoryId.remove(wsSessionId);
                closed.set(true);
            });
            return oldMessages;
        });
        return closed.get();
    }

    public boolean deleteMessages(String wsSessionId) {
        return messagesByMemoryId.remove(wsSessionId) != null;
    }

    /**
     * 规范化消息列表，确保符合LLM模型要求
     * 1. 系统消息必须在第一条且最多只能有一条
     * 2. 用户消息后面不能连续出现第二条用户消息
     * 3. AI消息的ToolExecutionRequest必须有对应的ToolExecutionResultMessage
     */
    private List<ChatMessage> normalizeMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        List<ChatMessage> normalized = new CopyOnWriteArrayList<>();

        // 第一步：处理系统消息，确保只有一条且在第一位
        SystemMessage systemMessage = null;
        for (ChatMessage message : messages) {
            if (message instanceof SystemMessage) {
                systemMessage = (SystemMessage) message;
                break; // 只保留第一条系统消息
            }
        }

        if (systemMessage != null) {
            normalized.add(systemMessage);
        }

        // 第二步：处理其他消息，并修复连续用户消息
        ChatMessage previousMessage = systemMessage;

        for (ChatMessage message : messages) {
            if (message instanceof SystemMessage) {
                continue; // 跳过系统消息，已经处理过了
            }

            // 检查连续的用户消息
            if (message instanceof UserMessage && previousMessage instanceof UserMessage) {
                // 在两条用户消息之间插入一条AI消息
                normalized.add(AiMessage.from("执行中断"));
            }

            normalized.add(message);
            previousMessage = message;
        }

        // 第三步：修复AI消息的工具调用结果
        return fixToolExecutionResults(normalized);
    }

    /**
     * 修复AI消息的工具调用结果
     * 确保每个ToolExecutionRequest都有对应的ToolExecutionResultMessage
     */
    private List<ChatMessage> fixToolExecutionResults(List<ChatMessage> messages) {
        List<ChatMessage> fixed = new CopyOnWriteArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            fixed.add(message);

            // 检查是否是带工具调用的AI消息
            if (message instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                List<dev.langchain4j.agent.tool.ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();

                // 收集后续的ToolExecutionResultMessage
                List<ToolExecutionResultMessage> toolResults = new CopyOnWriteArrayList<>();
                int j = i + 1;
                while (j < messages.size() && messages.get(j) instanceof ToolExecutionResultMessage) {
                    toolResults.add((ToolExecutionResultMessage) messages.get(j));
                    j++;
                }

                // 验证和修复工具调用结果
                List<ToolExecutionResultMessage> validResults = validateAndFixToolResults(toolRequests, toolResults);

                // 添加修复后的工具结果
                fixed.addAll(validResults);

                // 跳过原来的工具结果消息
                i = j - 1;
            }
        }

        return fixed;
    }

    /**
     * 验证和修复工具调用结果
     * 确保工具结果的数量、ID和顺序与工具请求一一对应
     *
     * @param toolRequests 工具调用请求列表
     * @param toolResults  现有的工具调用结果列表
     * @return 修复后的工具调用结果列表
     */
    private List<ToolExecutionResultMessage> validateAndFixToolResults(
            List<dev.langchain4j.agent.tool.ToolExecutionRequest> toolRequests,
            List<ToolExecutionResultMessage> toolResults) {

        List<ToolExecutionResultMessage> validResults = new CopyOnWriteArrayList<>();

        // 为每个工具请求找到或创建对应的结果
        for (dev.langchain4j.agent.tool.ToolExecutionRequest request : toolRequests) {
            String requestId = request.id();
            String toolName = request.name();

            // 查找匹配的工具结果
            ToolExecutionResultMessage matchedResult = null;
            for (ToolExecutionResultMessage result : toolResults) {
                if (requestId.equals(result.id()) && toolName.equals(result.toolName())) {
                    matchedResult = result;
                    break;
                }
            }

            if (matchedResult != null) {
                // 找到匹配的结果，直接使用
                validResults.add(matchedResult);
            } else {
                // 没有找到匹配的结果，创建一个"执行中断"的结果
                ToolExecutionResultMessage interruptedResult = ToolExecutionResultMessage.from(
                        requestId,
                        toolName,
                        "执行中断"
                );
                validResults.add(interruptedResult);
            }
        }

        return validResults;
    }

}
