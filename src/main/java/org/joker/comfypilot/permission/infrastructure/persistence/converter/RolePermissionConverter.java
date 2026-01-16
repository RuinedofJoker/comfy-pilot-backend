package org.joker.comfypilot.permission.infrastructure.persistence.converter;

import org.joker.comfypilot.permission.domain.entity.RolePermission;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePermissionPO;
import org.mapstruct.Mapper;

/**
 * 角色权限关联转换器
 */
@Mapper(componentModel = "spring")
public interface RolePermissionConverter {

    /**
     * PO转领域实体
     */
    RolePermission toDomain(RolePermissionPO po);

    /**
     * 领域实体转PO
     */
    RolePermissionPO toPO(RolePermission domain);
}
