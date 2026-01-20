package org.joker.comfypilot.agent.application.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * AgentConfig DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AgentConfigDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "agentScopeConfig", source = "agentScopeConfig", qualifiedByName = "mapToString")
    @Mapping(target = "config", source = "config", qualifiedByName = "mapToString")
    AgentConfigDTO toDTO(AgentConfig entity);

    @Named("statusToString")
    default String statusToString(org.joker.comfypilot.agent.domain.enums.AgentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize advancedFeatures", e);
        }
    }
}
