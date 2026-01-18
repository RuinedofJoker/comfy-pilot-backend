package org.joker.comfypilot.cfsvr.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServerAdvancedFeatures;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.po.ComfyuiServerPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * ComfyUI服务转换器（PO ↔ Entity）
 */
@Mapper(componentModel = "spring")
public interface ComfyuiServerConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "authMode", source = "authMode", qualifiedByName = "stringToAuthMode")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "stringToHealthStatus")
    @Mapping(target = "advancedFeatures", source = "advancedFeatures", qualifiedByName = "stringToAdvancedFeatures")
    ComfyuiServer toDomain(ComfyuiServerPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "authMode", source = "authMode", qualifiedByName = "authModeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
    @Mapping(target = "advancedFeatures", source = "advancedFeatures", qualifiedByName = "advancedFeaturesToString")
    ComfyuiServerPO toPO(ComfyuiServer domain);

    @Named("stringToAuthMode")
    default AuthMode stringToAuthMode(String value) {
        return AuthMode.fromCode(value);
    }

    @Named("authModeToString")
    default String authModeToString(AuthMode authMode) {
        return authMode != null ? authMode.getCode() : null;
    }

    @Named("stringToHealthStatus")
    default HealthStatus stringToHealthStatus(String value) {
        return value != null ? HealthStatus.valueOf(value) : null;
    }

    @Named("healthStatusToString")
    default String healthStatusToString(HealthStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToAdvancedFeatures")
    default ComfyuiServerAdvancedFeatures stringToAdvancedFeatures(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(json, ComfyuiServerAdvancedFeatures.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize advancedFeatures", e);
        }
    }

    @Named("advancedFeaturesToString")
    default String advancedFeaturesToString(ComfyuiServerAdvancedFeatures features) {
        if (features == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(features);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize advancedFeatures", e);
        }
    }
}
