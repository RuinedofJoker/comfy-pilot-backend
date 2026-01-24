package org.joker.comfypilot.resource.application.converter;

import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.interfaces.dto.FileResourceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "webPath", expression = "java(getWebPath(entity))")
    FileResourceDTO toDTO(FileResource entity);

    default String getWebPath(FileResource entity) {
        return switch (entity.getSourceType()) {
            case SERVER_LOCAL -> entity.getWebRelativePath();
            case null, default -> entity.getWebRelativePath();
        };
    }

}
