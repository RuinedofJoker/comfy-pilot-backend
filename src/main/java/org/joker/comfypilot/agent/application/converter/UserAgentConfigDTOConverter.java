package org.joker.comfypilot.agent.application.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.agent.application.dto.UserAgentConfigDTO;
import org.joker.comfypilot.agent.domain.entity.UserAgentConfig;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * UserAgentConfig DTO转换器
 */
@Mapper(componentModel = "spring")
public interface UserAgentConfigDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "mapToString")
    UserAgentConfigDTO toDTO(UserAgentConfig entity);

    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        try {
            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to deserialize advancedFeatures", e);
        }
    }

}
