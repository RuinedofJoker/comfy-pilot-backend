package org.joker.comfypilot.model.infrastructure.persistence.converter;

import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelProviderPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 模型提供商转换器
 */
@Mapper(componentModel = "spring")
public interface ModelProviderConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "providerType", source = "providerType", qualifiedByName = "stringToEnum")
    ModelProvider toDomain(ModelProviderPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "providerType", source = "providerType", qualifiedByName = "enumToString")
    ModelProviderPO toPO(ModelProvider domain);

    @Named("stringToEnum")
    default ProviderType stringToEnum(String value) {
        return value != null ? ProviderType.fromCode(value) : null;
    }

    @Named("enumToString")
    default String enumToString(ProviderType type) {
        return type != null ? type.getCode() : null;
    }
}
