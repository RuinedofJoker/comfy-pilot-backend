package org.joker.comfypilot.agent.domain.context;

/**
 * 工具执行上下文
 */
public class AgentExecutionContextHolder {

    private static final ThreadLocal<AgentExecutionContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前工具执行上下文
     *
     * @param executionContext 当前工具执行上下文
     */
    public static void set(AgentExecutionContext executionContext) {
        CONTEXT.set(executionContext);
    }

    /**
     * 获取当前工具执行上下文
     *
     * @return 用户会话
     */
    public static AgentExecutionContext get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前工具执行上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

}
