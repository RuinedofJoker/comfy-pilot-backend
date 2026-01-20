package org.joker.comfypilot.agent.application.converter;

import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.mapstruct.Mapper;

/**
 * AgentRuntimeConfig DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AgentRuntimeConfigDTOConverter {

    /**
     * Entity转DTO
     */
    AgentRuntimeConfigDTO toDTO(AgentConfig entity);

}
