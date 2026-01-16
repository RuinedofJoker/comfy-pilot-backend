package org.joker.comfypilot.permission.infrastructure.persistence.converter;

import org.joker.comfypilot.permission.domain.entity.UserRole;
import org.joker.comfypilot.permission.infrastructure.persistence.po.UserRolePO;
import org.mapstruct.Mapper;

/**
 * 用户角色关联转换器
 */
@Mapper(componentModel = "spring")
public interface UserRoleConverter {

    /**
     * PO转领域实体
     */
    UserRole toDomain(UserRolePO po);

    /**
     * 领域实体转PO
     */
    UserRolePO toPO(UserRole domain);
}
