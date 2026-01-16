package org.joker.comfypilot.model.application.converter;

import org.joker.comfypilot.model.application.dto.ModelProviderDTO;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型提供商DTO转换器
 */
@Mapper(componentModel = "spring")
public interface ModelProviderDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "providerType", source = "providerType.code")
    ModelProviderDTO toDTO(ModelProvider entity);
}
