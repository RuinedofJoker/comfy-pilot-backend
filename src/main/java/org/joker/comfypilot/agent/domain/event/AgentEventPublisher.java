package org.joker.comfypilot.agent.domain.event;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 事件发布器
 * 管理事件监听器的注册和事件的发布
 */
@Slf4j
public class AgentEventPublisher {

    /**
     * 事件监听器映射
     * Key: 事件类型
     * Value: 监听器列表
     */
    private final Map<AgentEventType, List<AgentEventListener<? extends AgentEvent>>> listeners = new ConcurrentHashMap<>();

    /**
     * 注册事件监听器
     *
     * @param eventType 事件类型
     * @param listener  监听器
     */
    public <T extends AgentEvent> void addEventListener(AgentEventType eventType, AgentEventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        log.debug("注册事件监听器: eventType={}", eventType);
    }

    /**
     * 移除事件监听器
     *
     * @param eventType 事件类型
     * @param listener  监听器
     */
    public <T extends AgentEvent> void removeEventListener(AgentEventType eventType, AgentEventListener<T> listener) {
        List<AgentEventListener<? extends AgentEvent>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            log.debug("移除事件监听器: eventType={}", eventType);
        }
    }

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    @SuppressWarnings("unchecked")
    public <T extends AgentEvent> void publishEvent(T event) {
        List<AgentEventListener<? extends AgentEvent>> eventListeners = listeners.get(event.getEventType());
        if (eventListeners != null && !eventListeners.isEmpty()) {
            log.debug("发布事件: eventType={}, listenerCount={}", event.getEventType(), eventListeners.size());
            for (AgentEventListener<? extends AgentEvent> listener : eventListeners) {
                try {
                    ((AgentEventListener<T>) listener).onEvent(event);

                    // 如果事件被取消，停止传播
                    if (event.isCancelled()) {
                        log.debug("事件已被取消，停止传播: eventType={}", event.getEventType());
                        break;
                    }
                } catch (Exception e) {
                    log.error("事件监听器执行失败: eventType={}, error={}", event.getEventType(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 清空所有监听器
     */
    public void clear() {
        listeners.clear();
        log.debug("清空所有事件监听器");
    }
}
