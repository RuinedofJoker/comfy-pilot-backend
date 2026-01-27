package org.joker.comfypilot.agent.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.agent.domain.entity.UserAgentConfig;
import org.joker.comfypilot.agent.infrastructure.persistence.po.UserAgentConfigPO;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * 用户Agent配置转换器（PO ↔ Entity）
 */
@Mapper(componentModel = "spring")
public interface UserAgentConfigConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "stringToMap")
    UserAgentConfig toDomain(UserAgentConfigPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "mapToString")
    UserAgentConfigPO toPO(UserAgentConfig domain);

    @Named("stringToMap")
    default Map<String, Object> stringToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to deserialize advancedFeatures", e);
        }
    }

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
