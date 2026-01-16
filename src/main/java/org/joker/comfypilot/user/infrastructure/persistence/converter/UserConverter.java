package org.joker.comfypilot.user.infrastructure.persistence.converter;

import org.joker.comfypilot.user.domain.entity.User;
import org.joker.comfypilot.user.infrastructure.persistence.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 用户实体转换器
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * PO转领域实体
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    @Mapping(target = "isDeleted", source = "isDeleted", qualifiedByName = "longToBoolean")
    User toDomain(UserPO po);

    /**
     * 领域实体转PO
     * 注意：映射所有字段，包括 BasePO 中的字段
     */
    @Mapping(target = "isDeleted", source = "isDeleted", qualifiedByName = "booleanToLong")
    UserPO toPO(User domain);

    /**
     * Long转Boolean（MyBatis-Plus逻辑删除字段转换）
     * isDeleted: 0-未删除，非0-已删除
     */
    @Named("longToBoolean")
    default Boolean longToBoolean(Long value) {
        return value != null && value > 0;
    }

    /**
     * Boolean转Long（MyBatis-Plus逻辑删除字段转换）
     * isDeleted: false-0（未删除），true-当前时间戳（已删除）
     */
    @Named("booleanToLong")
    default Long booleanToLong(Boolean value) {
        if (value == null || !value) {
            return 0L;
        }
        return System.currentTimeMillis();
    }
}
