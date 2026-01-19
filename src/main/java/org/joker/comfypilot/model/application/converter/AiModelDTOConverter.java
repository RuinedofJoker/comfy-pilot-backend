package org.joker.comfypilot.model.application.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * AI模型DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AiModelDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "accessType", source = "accessType.code")
    @Mapping(target = "modelType", source = "modelType.code")
    @Mapping(target = "modelCallingType", source = "modelCallingType.code")
    @Mapping(target = "modelSource", source = "modelSource.code")
    @Mapping(target = "providerType", source = "providerType.code")
    @Mapping(target = "modelConfig", source = "modelConfig", qualifiedByName = "mapToString")
    AiModelDTO toDTO(AiModel entity);

    @Named("stringToMap")
    default Map<String, Object> stringToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize advancedFeatures", e);
        }
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
