package org.joker.comfypilot.resource.infrastructure.persistence.converter;

import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.enums.FileSourceType;
import org.joker.comfypilot.resource.infrastructure.persistence.po.FileResourcePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 文件资源转换器
 */
@Mapper(componentModel = "spring")
public interface FileResourceConverter {

    /**
     * PO转领域实体
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "stringToEnum")
    FileResource toDomain(FileResourcePO po);

    /**
     * 领域实体转PO
     *
     * @param domain 领域实体
     * @return 持久化对象
     */
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "enumToString")
    FileResourcePO toPO(FileResource domain);

    /**
     * 字符串转枚举
     */
    @Named("stringToEnum")
    default FileSourceType stringToEnum(String value) {
        return value != null ? FileSourceType.valueOf(value) : null;
    }

    /**
     * 枚举转字符串
     */
    @Named("enumToString")
    default String enumToString(FileSourceType sourceType) {
        return sourceType != null ? sourceType.name() : null;
    }
}
