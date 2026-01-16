package org.joker.comfypilot.user.application.converter;

import org.joker.comfypilot.user.application.dto.UserDTO;
import org.joker.comfypilot.user.domain.entity.User;
import org.mapstruct.Mapper;

/**
 * 用户DTO转换器
 */
@Mapper(componentModel = "spring")
public interface UserDTOConverter {

    /**
     * Entity转DTO
     *
     * @param entity 领域实体
     * @return DTO对象
     */
    UserDTO toDTO(User entity);
}
