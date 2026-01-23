package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;

/**
 * Agent提示类型枚举
 * 用于统一管理Agent执行过程中的各种状态提示
 */
@Getter
public enum AgentPromptType {

    /**
     * Agent开始思考
     */
    THINKING("思考中", "Agent正在分析问题..."),

    /**
     * Agent正在调用工具
     */
    TOOL_CALLING("工具调用中", "Agent正在调用工具..."),

    /**
     * 工具调用完成，准备下一轮思考
     */
    TOOL_COMPLETE("工具完成", "工具调用已完成，继续分析..."),

    /**
     * Agent开始生成摘要
     */
    SUMMARY("生成摘要中", "Agent正在生成摘要..."),

    /**
     * 摘要生成完成
     */
    SUMMARY_COMPLETE("摘要完成", "摘要已生成完成"),

    /**
     * 执行被中断
     */
    INTERRUPTED("执行中断", "执行已被中断"),

    /**
     * 执行错误
     */
    ERROR("执行错误", "执行过程中发生错误");

    /**
     * 提示类型名称
     */
    private final String typeName;

    /**
     * 默认提示内容
     */
    private final String defaultMessage;

    AgentPromptType(String typeName, String defaultMessage) {
        this.typeName = typeName;
        this.defaultMessage = defaultMessage;
    }
}
