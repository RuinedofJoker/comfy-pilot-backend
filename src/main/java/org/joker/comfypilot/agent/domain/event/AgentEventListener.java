package org.joker.comfypilot.agent.domain.event;

/**
 * Agent 事件监听器接口
 * 使用泛型支持不同类型的事件
 */
@FunctionalInterface
public interface AgentEventListener<T extends AgentEvent> {

    /**
     * 处理事件
     *
     * @param event 事件对象
     */
    void onEvent(T event);
}
