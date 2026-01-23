package org.joker.comfypilot.agent.infrastructure.memory;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
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
        AtomicBoolean closed = new AtomicBoolean(false);
        messagesByMemoryId.compute(wsSessionId, (ignored, oldMessages) -> {
            if (oldMessages == null) {
                // 说明会话消息未初始化或会话已经关闭了
                closed.set(true);
            } else {
                if (chatMessage instanceof SystemMessage) {
                    if (!oldMessages.isEmpty() && (oldMessages.getFirst() instanceof SystemMessage)) {
                        oldMessages.removeFirst();
                        oldMessages.addFirst(chatMessage);
                    }
                } else {
                    oldMessages.add(chatMessage);
                }
            }
            return oldMessages;
        });
        return closed.get();
    }

    public boolean updateMessages(String wsSessionId, List<ChatMessage> messages) {
        AtomicBoolean closed = new AtomicBoolean(false);
        adjustMessages(messages);

        messagesByMemoryId.compute(wsSessionId, (ignored, oldMessages) -> {
            oldMessages = new CopyOnWriteArrayList<>(messages);
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
     * 调整历史消息防止模型执行报错
     */
    private void adjustMessages(List<ChatMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            switch (chatMessage) {
                case SystemMessage systemMessage -> {
                    // 系统消息必须做第一个出现且最多只能有一个
                    List<Integer> removedList = new ArrayList<>();
                    for (int j = i + 1; j < messages.size(); j++) {
                        ChatMessage afterMessage = messages.get(j);
                        if (afterMessage instanceof SystemMessage) {
                            messages.set(i, afterMessage);
                            removedList.add(j);
                        }
                    }
                    removedList = removedList.reversed();
                    for (int j : removedList) {
                        messages.remove(j);
                    }
                }
                case UserMessage userMessage -> {
                    List<Integer> removedList = new ArrayList<>();
                    UserMessage adjustUserMessage = adjustUserMessage(userMessage);

                    // 用户消息不能连续出现
                    if (adjustUserMessage != userMessage) {
                        messages.remove(i);
                        messages.add(i, adjustUserMessage);
                        userMessage = adjustUserMessage;
                    }
                    for (int j = i + 1; j < messages.size(); j++) {
                        ChatMessage afterMessage = messages.get(j);
                        if (afterMessage instanceof UserMessage afterUserMessage) {
                            adjustUserMessage = adjustUserMessage(afterUserMessage);
                            if (adjustUserMessage != afterUserMessage) {
                                messages.remove(j);
                                messages.add(j, adjustUserMessage);
                            }
                            removedList.add(j);
                        } else {
                            break;
                        }
                    }
                    if (!removedList.isEmpty()) {
                        removedList.addFirst(i);
                        StringBuilder userMessageText = new StringBuilder();
                        for (int userMessageIndex : removedList) {
                            userMessage = (UserMessage) messages.get(userMessageIndex);
                            if (!userMessageText.isEmpty()) {
                                userMessageText.append("\n");
                            }
                            userMessageText.append(((TextContent) userMessage.contents().getFirst()).text());
                        }
                        List<Content> contents = new ArrayList<>(userMessage.contents());
                        contents.removeFirst();
                        contents.addFirst(TextContent.from(userMessageText.toString()));
                        userMessage = UserMessage.from(contents);
                        removedList = removedList.reversed();
                        for (int j : removedList) {
                            messages.remove(j);
                        }
                        messages.add(i, userMessage);
                    }

                    // 用户消息后面必须跟一个ai消息
                    if (i == messages.size() - 1 || !(messages.get(i + 1) instanceof AiMessage)) {
                        messages.add(i + 1, AiMessage.from("执行被中断..."));
                    }
                }
                case AiMessage aiMessage -> {
                    // ai消息必须在用户消息之后出现
                    if (i == 0 || !(messages.get(i - 1) instanceof UserMessage)) {
                        messages.add(i, UserMessage.from("用户历史记录丢失..."));
                        i++;
                    }
                    int groupNext = i + 1;

                    // ai消息如果有工具执行请求，后面必须跟上相同数量的工具执行返回消息
                    if (aiMessage.hasToolExecutionRequests()) {
                        List<ToolExecutionRequest> toolExecutionRequests = aiMessage.toolExecutionRequests();
                        List<Integer> toolExecutionResultMessagesIndexs = new ArrayList<>();
                        Map<String, Integer> toolExecutionResultMessagesIdMap = new HashMap<>();
                        for (int j = i + 1; j < messages.size(); j++) {
                            if (messages.get(j) instanceof ToolExecutionResultMessage toolExecutionResultMessage) {
                                toolExecutionResultMessagesIndexs.add(j);
                                toolExecutionResultMessagesIdMap.put(toolExecutionResultMessage.id(), j);
                            } else {
                                break;
                            }
                        }
                        if (toolExecutionRequests.size() != toolExecutionResultMessagesIndexs.size() || toolExecutionResultMessagesIndexs.size() != toolExecutionResultMessagesIdMap.size()) {
                            Map<String, ToolExecutionResultMessage> toolExecutionResultMessagesMap = new HashMap<>();
                            toolExecutionResultMessagesIndexs.reversed();
                            for (int toolExecutionResultMessagesIndex : toolExecutionResultMessagesIndexs) {
                                ToolExecutionResultMessage toolExecutionResultMessage = (ToolExecutionResultMessage) messages.remove(toolExecutionResultMessagesIndex);
                                if (!toolExecutionResultMessagesMap.containsKey(toolExecutionResultMessage.id())) {
                                    toolExecutionResultMessagesMap.put(toolExecutionResultMessage.id(), toolExecutionResultMessage);
                                }
                            }
                            for (int i1 = 0; i1 < toolExecutionRequests.size(); i1++) {
                                ToolExecutionRequest toolExecutionRequest = toolExecutionRequests.get(i1);
                                ToolExecutionResultMessage toolExecutionResultMessage = toolExecutionResultMessagesMap.get(toolExecutionRequest.id());
                                if (toolExecutionResultMessage == null) {
                                    toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, "执行结果丢失...");
                                }
                                messages.add(i + 1 + i1, toolExecutionResultMessage);
                            }
                        }

                        groupNext += toolExecutionRequests.size();
                    }

                    // ai消息和执行工具返回后面必须是用户消息
                    List<Integer> removedList = new ArrayList<>();
                    for (int j = groupNext; j < messages.size(); j++) {
                        if (!(messages.get(j) instanceof UserMessage)) {
                            removedList.add(j);
                        } else {
                            break;
                        }
                    }
                    removedList = removedList.reversed();
                    for (int j : removedList) {
                        messages.remove(j);
                    }
                }
                case ToolExecutionResultMessage toolExecutionResultMessage -> {
                    // 工具执行结果不允许单独出现
                    if (i == 0 || (!(messages.get(i - 1) instanceof AiMessage) && !(messages.get(i - 1) instanceof ToolExecutionResultMessage))) {
                        messages.remove(i);
                    }
                }
                case null, default ->
                    // 不允许出现的消息
                        messages.remove(i);
            }
        }
    }

    private UserMessage adjustUserMessage(UserMessage userMessage) {
        if (userMessage.contents() == null || userMessage.contents().isEmpty()) {
            return UserMessage.from("");
        }
        StringBuilder textContentText = new StringBuilder();
        int textContentIndex = -1;
        for (int i1 = 0; i1 < userMessage.contents().size(); i1++) {
            Content content = userMessage.contents().get(i1);
            if (content instanceof TextContent textContent) {
                if (!textContentText.isEmpty()) {
                    textContentText.append("\n");
                }
                textContentText.append(textContent.text());
                textContentIndex = i1;
            }
        }
        if (textContentIndex != 0) {
            List<Content> contents = new ArrayList<>(userMessage.contents());
            contents.removeIf(content -> content instanceof TextContent);
            contents.addFirst(TextContent.from(textContentText.toString()));
            return UserMessage.from(contents);
        }
        return userMessage;
    }
}
