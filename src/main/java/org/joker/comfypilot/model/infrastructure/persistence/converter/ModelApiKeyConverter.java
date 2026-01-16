package org.joker.comfypilot.model.infrastructure.persistence.converter;

import org.joker.comfypilot.model.domain.entity.ModelApiKey;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelApiKeyPO;
import org.mapstruct.Mapper;

/**
 * 模型API密钥转换器
 */
@Mapper(componentModel = "spring")
public interface ModelApiKeyConverter {

    /**
     * PO转领域实体
     */
    ModelApiKey toDomain(ModelApiKeyPO po);

    /**
     * 领域实体转PO
     */
    ModelApiKeyPO toPO(ModelApiKey domain);
}
