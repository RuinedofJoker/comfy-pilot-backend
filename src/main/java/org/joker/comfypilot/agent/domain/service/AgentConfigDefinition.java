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

}
