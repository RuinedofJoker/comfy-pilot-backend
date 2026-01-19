package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerPublicDTO;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * ComfyUI服务公开DTO转换器（Entity → PublicDTO）
 */
@Mapper(componentModel = "spring")
public interface ComfyuiServerPublicDTOConverter {

    /**
     * Entity转公开DTO（过滤敏感信息）
     */
    @Mapping(target = "authMode", source = "authMode", qualifiedByName = "authModeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
    ComfyuiServerPublicDTO toPublicDTO(ComfyuiServer entity);

    @Named("authModeToString")
    default String authModeToString(AuthMode authMode) {
        return authMode != null ? authMode.getCode() : null;
    }

    @Named("healthStatusToString")
    default String healthStatusToString(HealthStatus status) {
        return status != null ? status.name() : null;
    }
}
