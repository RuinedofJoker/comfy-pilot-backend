package org.joker.comfypilot.resource.application.converter;

import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.interfaces.dto.FileResourceDTO;
import org.mapstruct.Mapper;

/**
 * 文件资源DTO转换器
 */
@Mapper(componentModel = "spring")
public interface FileResourceDTOConverter {

    /**
     * Entity转DTO
     *
     * @param entity 领域实体
     * @return DTO对象
     */
    FileResourceDTO toDTO(FileResource entity);
}
