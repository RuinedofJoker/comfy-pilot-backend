package org.joker.comfypilot.resource.infrastructure.persistence.converter;

import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.infrastructure.persistence.po.FileResourcePO;
import org.mapstruct.Mapper;

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
    FileResource toDomain(FileResourcePO po);

    /**
     * 领域实体转PO
     *
     * @param domain 领域实体
     * @return 持久化对象
     */
    FileResourcePO toPO(FileResource domain);
}
