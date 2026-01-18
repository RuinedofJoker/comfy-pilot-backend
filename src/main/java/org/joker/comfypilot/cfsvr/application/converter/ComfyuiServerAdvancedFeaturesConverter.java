package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerAdvancedFeaturesDTO;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServerAdvancedFeatures;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * ComfyUI服务器高级功能配置转换器
 */
@Mapper(componentModel = "spring", uses = {SshConnectionConfigConverter.class, ComfyuiDirectoryConfigConverter.class})
public interface ComfyuiServerAdvancedFeaturesConverter {

    /**
     * DTO转Entity
     */
    @Mapping(target = "connectionType", source = "connectionType", qualifiedByName = "stringToConnectionType")
    @Mapping(target = "osType", source = "osType", qualifiedByName = "stringToOsType")
    ComfyuiServerAdvancedFeatures toEntity(ComfyuiServerAdvancedFeaturesDTO dto);

    /**
     * Entity转DTO
     */
    @Mapping(target = "connectionType", source = "connectionType", qualifiedByName = "connectionTypeToString")
    @Mapping(target = "osType", source = "osType", qualifiedByName = "osTypeToString")
    ComfyuiServerAdvancedFeaturesDTO toDTO(ComfyuiServerAdvancedFeatures entity);

    @Named("stringToConnectionType")
    default org.joker.comfypilot.cfsvr.domain.enums.ConnectionType stringToConnectionType(String value) {
        return value != null ? org.joker.comfypilot.cfsvr.domain.enums.ConnectionType.valueOf(value) : null;
    }

    @Named("connectionTypeToString")
    default String connectionTypeToString(org.joker.comfypilot.cfsvr.domain.enums.ConnectionType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToOsType")
    default org.joker.comfypilot.cfsvr.domain.enums.OsType stringToOsType(String value) {
        return value != null ? org.joker.comfypilot.cfsvr.domain.enums.OsType.valueOf(value) : null;
    }

    @Named("osTypeToString")
    default String osTypeToString(org.joker.comfypilot.cfsvr.domain.enums.OsType type) {
        return type != null ? type.name() : null;
    }
}
