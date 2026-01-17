package org.joker.comfypilot.agent.application.converter;

import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * AgentConfig DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AgentConfigDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    AgentConfigDTO toDTO(AgentConfig entity);

    @Named("statusToString")
    default String statusToString(org.joker.comfypilot.agent.domain.enums.AgentStatus status) {
        return status != null ? status.name() : null;
    }
}
