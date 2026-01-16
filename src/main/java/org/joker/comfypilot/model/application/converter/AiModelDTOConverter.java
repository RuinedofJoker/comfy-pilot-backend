package org.joker.comfypilot.model.application.converter;

import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AI模型DTO转换器
 */
@Mapper(componentModel = "spring")
public interface AiModelDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "accessType", source = "accessType.code")
    @Mapping(target = "modelType", source = "modelType.code")
    @Mapping(target = "modelSource", source = "modelSource.code")
    AiModelDTO toDTO(AiModel entity);
}
