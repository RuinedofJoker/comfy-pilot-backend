package org.joker.comfypilot.agent.infrastructure.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
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
        return messagesByMemoryId.computeIfAbsent(wsSessionId, ignored -> {
            // 说明会话消息未初始化或会话已经关闭了
            return null;
        });
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
