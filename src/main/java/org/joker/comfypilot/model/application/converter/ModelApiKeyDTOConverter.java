package org.joker.comfypilot.model.application.converter;

import org.joker.comfypilot.model.application.dto.ModelApiKeyDTO;
import org.joker.comfypilot.model.domain.entity.ModelApiKey;
import org.mapstruct.Mapper;

/**
 * 模型API密钥DTO转换器
 */
@Mapper(componentModel = "spring")
public interface ModelApiKeyDTOConverter {

    /**
     * Entity转DTO
     */
    ModelApiKeyDTO toDTO(ModelApiKey entity);
}
