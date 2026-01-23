package org.joker.comfypilot.agent.infrastructure.memory;

import dev.langchain4j.data.message.*;
import org.joker.comfypilot.common.util.HistoryMessageUtil;
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
                // 没执行进来说明会话消息未初始化或会话已经关闭了
                if (chatMessage instanceof SystemMessage) {
                    if (!oldMessages.isEmpty() && (oldMessages.getFirst() instanceof SystemMessage)) {
                        oldMessages.removeFirst();
                    }
                    oldMessages.addFirst(chatMessage);
                    added.set(true);
                } else if (chatMessage instanceof UserMessage userMessage) {
                    userMessage = HistoryMessageUtil.adjustUserMessage(userMessage);
                    if (oldMessages.isEmpty() || (oldMessages.size() == 1 && (oldMessages.getFirst() instanceof SystemMessage)) || oldMessages.getLast() instanceof AiMessage || oldMessages.getLast() instanceof ToolExecutionResultMessage) {
                        oldMessages.add(userMessage);
                        added.set(true);
                    } else if (oldMessages.getLast() instanceof UserMessage oldUserMessage) {
                        // 合并UserMessage
                        List<Content> contents = new ArrayList<>(userMessage.contents());
                        String text = ((TextContent) contents.removeFirst()).text();
                        String oldText = ((TextContent) (oldUserMessage).contents().getFirst()).text();
                        TextContent mergedTextContent = TextContent.from(oldText + "\n" + text);
                        contents.addFirst(mergedTextContent);
                        UserMessage mergedUserMessage = UserMessage.from(contents);
                        oldMessages.removeLast();
                        oldMessages.addLast(mergedUserMessage);
                        added.set(true);
                    }
                } else if (chatMessage instanceof AiMessage) {
                    if (!oldMessages.isEmpty() && (oldMessages.getFirst() instanceof UserMessage)) {
                        oldMessages.add(chatMessage);
                        added.set(true);
                    }
                } else if (chatMessage instanceof ToolExecutionResultMessage) {
                    if (!oldMessages.isEmpty() && ((oldMessages.getFirst() instanceof AiMessage) || (oldMessages.getFirst() instanceof ToolExecutionResultMessage))) {
                        oldMessages.add(chatMessage);
                        added.set(true);
                    }
                }
            }
            return oldMessages;
        });
        return added.get();
    }

    public boolean updateMessages(String wsSessionId, List<ChatMessage> messages) {
        AtomicBoolean closed = new AtomicBoolean(false);
        HistoryMessageUtil.adjustMessages(messages);

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


}
