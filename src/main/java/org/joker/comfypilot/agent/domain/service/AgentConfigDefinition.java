package org.joker.comfypilot.agent.domain.service;

import io.swagger.v3.oas.annotations.media.Schema;
import org.joker.comfypilot.agent.domain.enums.AgentConfigType;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;

@Schema(description = "Agent配置定义")
public record AgentConfigDefinition(
        @Schema(description = "配置名/键", example = "modelIdentifier")
        String name,

        @Schema(description = "配置描述", example = "使用的模型唯一标识符")
        String description,

        @Schema(description = "是否必填", example = "true")
        Boolean require,

        @Schema(description = "用户是否能覆盖", example = "true")
        Boolean userOverride,

        @Schema(description = "配置值类型")
        AgentConfigType type,

        @Schema(description = "字符串格式（正则表达式）", example = "^[a-zA-Z0-9_-]+$")
        String format,

        @Schema(description = "整数类型取值范围起始值", example = "0")
        Integer intStartScope,

        @Schema(description = "整数类型取值范围结束值", example = "10")
        Integer intEndScope,

        @Schema(description = "浮点数类型取值范围起始值", example = "0.0")
        Double floatStartScope,

        @Schema(description = "浮点数类型取值范围结束值", example = "1.0")
        Double floatEndScope,

        @Schema(description = "模型调用方式")
        String modelCallingType
) {

    /**
     * 创建模型类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @param modelCallingType 模型调用方式
     * @return 模型类型配置定义
     */
    public static AgentConfigDefinition modelValue(String name, String description, Boolean require, Boolean userOverride, ModelCallingType modelCallingType) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.MODEL, null, null, null, null, null, modelCallingType.getCode());
    }

    /**
     * 创建字符串类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @param format 字符串格式（正则表达式）
     * @return 字符串类型配置定义
     */
    public static AgentConfigDefinition stringValue(String name, String description, Boolean require, Boolean userOverride, String format) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.STRING, format, null, null, null, null, null);
    }

    /**
     * 创建文本类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @return 字符串类型配置定义
     */
    public static AgentConfigDefinition textValue(String name, String description, Boolean require, Boolean userOverride) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.TEXT, null, null, null, null, null, null);
    }

    /**
     * 创建整数类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @param startScope 取值范围起始值
     * @param endScope 取值范围结束值
     * @return 整数类型配置定义
     */
    public static AgentConfigDefinition intValue(String name, String description, Boolean require, Boolean userOverride, Integer startScope, Integer endScope) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.INT, null, startScope, endScope, null, null, null);
    }

    /**
     * 创建浮点数类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @param startScope 取值范围起始值
     * @param endScope 取值范围结束值
     * @return 浮点数类型配置定义
     */
    public static AgentConfigDefinition floatValue(String name, String description, Boolean require, Boolean userOverride, Double startScope, Double endScope) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.FLOAT, null, null, null, startScope, endScope, null);
    }

    /**
     * 创建布尔类型配置定义
     *
     * @param name 配置名
     * @param description 配置描述
     * @param require 是否必填
     * @param userOverride 用户是否能覆盖
     * @return 布尔类型配置定义
     */
    public static AgentConfigDefinition booleanValue(String name, String description, Boolean require, Boolean userOverride) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.BOOLEAN, null, null, null, null, null, null);
    }

}
