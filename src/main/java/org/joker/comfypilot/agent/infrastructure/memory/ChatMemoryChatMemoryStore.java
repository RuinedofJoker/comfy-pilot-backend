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
        AtomicBoolean closed = new AtomicBoolean(false);

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            if (chatMessage instanceof UserMessage && (i == messages.size() - 1 || !(messages.get(i + 1) instanceof AiMessage))) {
                messages.add(i + 1, AiMessage.from("用户中断对话"));
            }
        }

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
