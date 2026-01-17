package org.joker.comfypilot.agent.infrastructure.persistence.converter;

import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentExecutionLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Agent执行日志 PO 转换器
 */
@Mapper(componentModel = "spring")
public interface AgentExecutionLogConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToExecutionStatus")
    AgentExecutionLog toDomain(AgentExecutionLogPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "executionStatusToString")
    AgentExecutionLogPO toPO(AgentExecutionLog domain);

    /**
     * 字符串转ExecutionStatus枚举
     */
    @Named("stringToExecutionStatus")
    default ExecutionStatus stringToExecutionStatus(String value) {
        return value != null ? ExecutionStatus.valueOf(value) : null;
    }

    /**
     * ExecutionStatus枚举转字符串
     */
    @Named("executionStatusToString")
    default String executionStatusToString(ExecutionStatus status) {
        return status != null ? status.name() : null;
    }
}
