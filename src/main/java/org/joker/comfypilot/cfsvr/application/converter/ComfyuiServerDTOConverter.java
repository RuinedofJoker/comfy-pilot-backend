package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * ComfyUI服务DTO转换器（Entity ↔ DTO）
 */
@Mapper(componentModel = "spring", uses = {ComfyuiServerAdvancedFeaturesConverter.class})
public interface ComfyuiServerDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "authMode", source = "authMode", qualifiedByName = "authModeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
    ComfyuiServerDTO toDTO(ComfyuiServer entity);

    @Named("authModeToString")
    default String authModeToString(AuthMode authMode) {
        return authMode != null ? authMode.getCode() : null;
    }

    @Named("healthStatusToString")
    default String healthStatusToString(HealthStatus status) {
        return status != null ? status.name() : null;
    }
}
