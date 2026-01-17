package org.joker.comfypilot.agent.infrastructure.persistence.converter;

import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.enums.AgentStatus;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Agent配置 PO 转换器
 */
@Mapper(componentModel = "spring")
public interface AgentConfigConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToAgentStatus")
    AgentConfig toDomain(AgentConfigPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "agentStatusToString")
    AgentConfigPO toPO(AgentConfig domain);

    /**
     * 字符串转AgentStatus枚举
     */
    @Named("stringToAgentStatus")
    default AgentStatus stringToAgentStatus(String value) {
        return value != null ? AgentStatus.valueOf(value) : null;
    }

    /**
     * AgentStatus枚举转字符串
     */
    @Named("agentStatusToString")
    default String agentStatusToString(AgentStatus status) {
        return status != null ? status.name() : null;
    }
}
