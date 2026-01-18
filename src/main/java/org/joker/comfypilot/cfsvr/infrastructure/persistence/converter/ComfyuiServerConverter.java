package org.joker.comfypilot.cfsvr.infrastructure.persistence.converter;

import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
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
    ComfyuiServer toDomain(ComfyuiServerPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "authMode", source = "authMode", qualifiedByName = "authModeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
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
}
