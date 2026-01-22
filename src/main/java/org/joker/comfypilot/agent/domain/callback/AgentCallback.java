package org.joker.comfypilot.agent.domain.callback;

/**
 * Agent输出回调接口
 */
public interface AgentCallback {

    /**
     * 当Agent开始思考时调用
     */
    void onThinking();

    /**
     * 当Agent开始生成摘要时
     */
    void onSummery();

    /**
     * 当Agent生成完摘要后调用
     *
     * @param summery 摘要内容
     */
    void onSummeryComplete(String summery);

    /**
     * 当Agent输出部分内容时调用（流式输出）
     *
     * @param chunk 部分内容
     */
    void onStream(String chunk);

    /**
     * 当Agent完成输出时调用
     *
     * @param fullContent 完整内容
     */
    void onComplete(String fullContent);

    /**
     * 当Agent完成流式输出时调用
     *
     * @param fullContent 完整内容
     */
    void onStreamComplete(String fullContent);

    /**
     * 当Agent调用Tool时调用
     *
     * @param toolName 工具名称
     * @param toolArgs 工具参数
     */
    void onToolCall(String toolName, String toolArgs);

    /**
     * 执行中断完成调用
     */
    void onExecutionInterrupted();

    /**
     * 当发生错误时调用
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 检查是否被中断
     *
     * @return true表示已中断
     */
    boolean isInterrupted();
}
