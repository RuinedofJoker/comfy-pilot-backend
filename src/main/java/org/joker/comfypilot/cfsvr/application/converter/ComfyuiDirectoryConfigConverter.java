package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiDirectoryConfigDTO;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiDirectoryConfig;
import org.mapstruct.Mapper;

/**
 * ComfyUI目录配置转换器
 */
@Mapper(componentModel = "spring")
public interface ComfyuiDirectoryConfigConverter {

    /**
     * DTO转Entity
     */
    ComfyuiDirectoryConfig toEntity(ComfyuiDirectoryConfigDTO dto);

    /**
     * Entity转DTO
     */
    ComfyuiDirectoryConfigDTO toDTO(ComfyuiDirectoryConfig entity);
}
