package org.joker.comfypilot.model.application.converter;

import org.joker.comfypilot.model.application.dto.AiModelSimpleDTO;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AI模型简化DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AiModelSimpleDTOConverter {

    /**
     * Entity转SimpleDTO
     */
    @Mapping(target = "modelType", source = "modelType.code")
    @Mapping(target = "modelCallingType", source = "modelCallingType.code")
    AiModelSimpleDTO toSimpleDTO(AiModel entity);
}
