package org.joker.comfypilot.agent.domain.service;

import org.joker.comfypilot.agent.domain.enums.AgentConfigType;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;

public record AgentConfigDefinition(
        // 配置名/键
        String name,
        // 配置描述
        String description,
        // 是否必填
        Boolean require,
        // 用户是否能覆盖
        Boolean userOverride,
        // 配置值类型
        AgentConfigType type,
        // 字符串格式
        String format,
        // 浮点数类型取值范围
        Integer intStartScope,
        Integer intEndScope,
        // 浮点数类型取值范围
        Double floatStartScope,
        Double floatEndScope,
        // 模型调用方式
        ModelCallingType modelCallingType
) {

    public static AgentConfigDefinition modelValue(String name, String description, Boolean require, Boolean userOverride, ModelCallingType modelCallingType) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.MODEL, null, null, null, null, null, modelCallingType);
    }

    public static AgentConfigDefinition stringValue(String name, String description, Boolean require, Boolean userOverride, String format) {
        return new AgentConfigDefinition(name, description, require, userOverride, AgentConfigType.STRING, format, null, null, null, null, null);
    }

}
