package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * ComfyUI服务DTO转换器（Entity ↔ DTO）
 */
@Mapper(componentModel = "spring")
public interface ComfyuiServerDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "sourceTypeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
    ComfyuiServerDTO toDTO(ComfyuiServer entity);

    @Named("sourceTypeToString")
    default String sourceTypeToString(org.joker.comfypilot.cfsvr.domain.enums.ServerSourceType sourceType) {
        return sourceType != null ? sourceType.name() : null;
    }

    @Named("healthStatusToString")
    default String healthStatusToString(org.joker.comfypilot.cfsvr.domain.enums.HealthStatus status) {
        return status != null ? status.name() : null;
    }
}
