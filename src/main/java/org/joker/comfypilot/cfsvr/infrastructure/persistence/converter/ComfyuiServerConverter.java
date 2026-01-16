package org.joker.comfypilot.cfsvr.infrastructure.persistence.converter;

import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.domain.enums.ServerSourceType;
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
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "stringToSourceType")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "stringToHealthStatus")
    ComfyuiServer toDomain(ComfyuiServerPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "sourceTypeToString")
    @Mapping(target = "healthStatus", source = "healthStatus", qualifiedByName = "healthStatusToString")
    ComfyuiServerPO toPO(ComfyuiServer domain);

    @Named("stringToSourceType")
    default ServerSourceType stringToSourceType(String value) {
        return value != null ? ServerSourceType.valueOf(value) : null;
    }

    @Named("sourceTypeToString")
    default String sourceTypeToString(ServerSourceType sourceType) {
        return sourceType != null ? sourceType.name() : null;
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
