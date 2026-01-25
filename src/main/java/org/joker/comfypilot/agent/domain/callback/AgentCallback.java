package org.joker.comfypilot.agent.domain.callback;

import dev.langchain4j.data.message.ChatMessage;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Agent输出回调接口
 */
public interface AgentCallback {

    /**
     * 统一的提示消息回调
     * 用于处理Agent执行过程中的各种状态提示
     *
     * @param promptType 提示类型
     * @param message    提示内容（可选，如果为null则使用默认提示）
     */
    void onPrompt(AgentPromptType promptType, String message);

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
     * 当Agent处理了中断时返回
     */
    void onInterrupted();

    /**
     * 当Agent调用Tool时调用
     *
     * @param isClientTool 是否是客户端工具
     * @param isMcpTool    是否是MCP工具
     * @param toolCallId   工具调用ID
     * @param toolName     工具名称
     * @param toolArgs     工具参数
     */
    void onToolCall(boolean isClientTool, boolean isMcpTool, String toolCallId, String toolName, String toolArgs);

    /**
     * 检查是否被中断
     *
     * @return true表示已中断
     */
    boolean isInterrupted();

    /**
     * 添加消息
     */
    void addMemoryMessage(ChatMessage message, Consumer<ChatMessage> successCallback, Consumer<ChatMessage> failCallback);

    /**
     * 获取历史消息
     */
    List<ChatMessage> getMemoryMessages();
}
