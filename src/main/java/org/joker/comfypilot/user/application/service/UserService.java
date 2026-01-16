package org.joker.comfypilot.user.application.service;

import org.joker.comfypilot.user.application.dto.UpdateUserRequest;
import org.joker.comfypilot.user.application.dto.UserDTO;

/**
 * 用户应用服务接口
 */
public interface UserService {

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    UserDTO getCurrentUser(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserDTO updateUser(Long userId, UpdateUserRequest request);
}
