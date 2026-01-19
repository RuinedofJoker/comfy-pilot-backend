package org.joker.comfypilot.model.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelSource;
import org.joker.comfypilot.model.domain.enums.ModelType;
import org.joker.comfypilot.model.infrastructure.persistence.po.AiModelPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * AI模型转换器
 */
@Mapper(componentModel = "spring")
public interface AiModelConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "accessType", source = "accessType", qualifiedByName = "stringToAccessTypeEnum")
    @Mapping(target = "modelType", source = "modelType", qualifiedByName = "stringToModelTypeEnum")
    @Mapping(target = "modelSource", source = "modelSource", qualifiedByName = "stringToModelSourceEnum")
    @Mapping(target = "modelConfig", source = "modelConfig", qualifiedByName = "stringToMap")
    AiModel toDomain(AiModelPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "accessType", source = "accessType", qualifiedByName = "accessTypeEnumToString")
    @Mapping(target = "modelType", source = "modelType", qualifiedByName = "modelTypeEnumToString")
    @Mapping(target = "modelSource", source = "modelSource", qualifiedByName = "modelSourceEnumToString")
    @Mapping(target = "modelConfig", source = "modelConfig", qualifiedByName = "mapToString")
    AiModelPO toPO(AiModel domain);

    @Named("stringToAccessTypeEnum")
    default ModelAccessType stringToAccessTypeEnum(String value) {
        return value != null ? ModelAccessType.fromCode(value) : null;
    }

    @Named("accessTypeEnumToString")
    default String accessTypeEnumToString(ModelAccessType type) {
        return type != null ? type.getCode() : null;
    }

    @Named("stringToModelTypeEnum")
    default ModelType stringToModelTypeEnum(String value) {
        return value != null ? ModelType.fromCode(value) : null;
    }

    @Named("modelTypeEnumToString")
    default String modelTypeEnumToString(ModelType type) {
        return type != null ? type.getCode() : null;
    }

    @Named("stringToModelSourceEnum")
    default ModelSource stringToModelSourceEnum(String value) {
        return value != null ? ModelSource.fromCode(value) : null;
    }

    @Named("modelSourceEnumToString")
    default String modelSourceEnumToString(ModelSource source) {
        return source != null ? source.getCode() : null;
    }

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
