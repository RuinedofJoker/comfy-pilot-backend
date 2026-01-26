package org.joker.comfypilot.agent.infrastructure.memory;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
                // 在添加新消息前，先规范化最后几条消息
                normalizeLastMessages(oldMessages, chatMessage);

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
                List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();

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
            List<ToolExecutionRequest> toolRequests,
            List<ToolExecutionResultMessage> toolResults) {

        List<ToolExecutionResultMessage> validResults = new CopyOnWriteArrayList<>();

        // 为每个工具请求找到或创建对应的结果
        for (ToolExecutionRequest request : toolRequests) {
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

    /**
     * 规范化最后几条消息（性能优化版本）
     * 只检查和修复可能存在问题的最后几条消息：
     * 1. 最后一条是UserMessage，且新消息不是AiMessage - 插入AI中断消息
     * 2. 最后一条是带工具调用的AiMessage，但缺少工具执行结果 - 补充工具结果
     * 3. 最后几条是ToolExecutionResultMessage，但对应的AiMessage缺少工具请求 - 补充占位结果
     *
     * @param oldMessages 现有消息列表
     * @param newMessage  即将添加的新消息
     */
    private void normalizeLastMessages(List<ChatMessage> oldMessages, ChatMessage newMessage) {
        if (oldMessages == null || oldMessages.isEmpty()) {
            return;
        }

        ChatMessage lastMessage = oldMessages.getLast();

        // 场景1：最后一条是UserMessage，且新消息不是AiMessage
        // 需要在两条UserMessage之间插入AI中断消息
        if (lastMessage instanceof UserMessage && !(newMessage instanceof AiMessage)) {
            oldMessages.add(AiMessage.from("用户中断对话"));
            return;
        }

        // 场景2：最后一条是带工具调用的AiMessage
        // 需要检查是否有对应的工具执行结果
        if (lastMessage instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
            fixMissingToolResults(oldMessages, aiMessage, newMessage);
            return;
        }

        // 场景3：最后几条是ToolExecutionResultMessage
        // 需要检查是否有对应的工具请求
        if (lastMessage instanceof ToolExecutionResultMessage) {
            fixOrphanedToolResults(oldMessages);
        }
    }

    /**
     * 修复缺失的工具执行结果
     * 当最后一条消息是带工具调用的AiMessage时，检查后续是否有对应的工具结果
     * 如果新消息不是ToolExecutionResultMessage，则为所有工具请求补充"执行中断"结果
     *
     * @param oldMessages 现有消息列表
     * @param aiMessage   带工具调用的AI消息
     * @param newMessage  即将添加的新消息
     */
    private void fixMissingToolResults(List<ChatMessage> oldMessages, AiMessage aiMessage, ChatMessage newMessage) {
        // 如果新消息是工具执行结果，说明正常流程，不需要修复
        if (newMessage instanceof ToolExecutionResultMessage) {
            return;
        }

        List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
        
        List<ChatMessage> needAddedMessages = new ArrayList<>(toolRequests.size());

        // 为每个工具请求创建"执行中断"的结果
        for (ToolExecutionRequest request : toolRequests) {
            ToolExecutionResultMessage interruptedResult = ToolExecutionResultMessage.from(
                    request.id(),
                    request.name(),
                    "执行中断"
            );
            needAddedMessages.add(interruptedResult);
        }
        oldMessages.addAll(needAddedMessages);
    }

    /**
     * 修复缺失的工具执行结果
     * 当最后几条消息是ToolExecutionResultMessage时，向前查找对应的AiMessage
     * 检查该AiMessage的所有工具请求是否都有对应的结果，如果缺少则补充"执行中断"结果
     *
     * @param oldMessages 现有消息列表
     */
    private void fixOrphanedToolResults(List<ChatMessage> oldMessages) {
        if (oldMessages.size() < 2) {
            return;
        }

        // 从后向前收集连续的ToolExecutionResultMessage
        List<ToolExecutionResultMessage> trailingToolResults = new ArrayList<>();
        int lastIndex = oldMessages.size() - 1;

        for (int i = lastIndex; i >= 0; i--) {
            ChatMessage message = oldMessages.get(i);
            if (message instanceof ToolExecutionResultMessage toolResult) {
                trailingToolResults.addFirst(toolResult);
            } else {
                break;
            }
        }

        if (trailingToolResults.isEmpty()) {
            return;
        }

        // 继续向前查找最近的AiMessage（应该就在工具结果前面）
        int searchStartIndex = lastIndex - trailingToolResults.size();
        AiMessage correspondingAiMessage = null;

        for (int i = searchStartIndex; i >= 0; i--) {
            ChatMessage message = oldMessages.get(i);
            if (message instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                correspondingAiMessage = aiMessage;
                break;
            }
        }

        // 如果找不到对应的AiMessage，说明数据异常，移除这些孤立的工具结果
        if (correspondingAiMessage == null) {
            for (ToolExecutionResultMessage orphanedResult : trailingToolResults) {
                oldMessages.remove(orphanedResult);
            }
            return;
        }

        // 检查是否所有工具请求都有对应的结果
        List<ToolExecutionRequest> toolRequests = correspondingAiMessage.toolExecutionRequests();

        for (ToolExecutionRequest request : toolRequests) {
            String requestId = request.id();
            String toolName = request.name();

            // 查找是否有匹配的工具结果
            boolean hasMatchingResult = trailingToolResults.stream()
                    .anyMatch(result -> requestId.equals(result.id())
                            && toolName.equals(result.toolName()));

            // 如果缺少对应的结果，补充"执行中断"结果
            if (!hasMatchingResult) {
                ToolExecutionResultMessage interruptedResult = ToolExecutionResultMessage.from(
                        requestId,
                        toolName,
                        "执行中断"
                );
                oldMessages.add(interruptedResult);
            }
        }
    }

}
