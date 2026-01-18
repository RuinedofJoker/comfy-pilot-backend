package org.joker.comfypilot.user.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.user.application.converter.UserDTOConverter;
import org.joker.comfypilot.user.application.dto.UpdateUserRequest;
import org.joker.comfypilot.user.application.dto.UserDTO;
import org.joker.comfypilot.user.application.service.UserService;
import org.joker.comfypilot.user.domain.entity.User;
import org.joker.comfypilot.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDTOConverter dtoConverter;

    @Override
    public UserDTO getCurrentUser(Long userId) {
        log.info("获取用户信息, userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return dtoConverter.toDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        log.info("更新用户信息, userId: {}, request: {}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 更新用户名
        if (request.getUsername() != null) {
            user.updateUsername(request.getUsername());
        }

        // 更新头像URL
        if (request.getAvatarUrl() != null) {
            user.updateAvatarUrl(request.getAvatarUrl());
        }

        // 保存更新
        User updatedUser = userRepository.save(user);

        log.info("用户信息更新成功, userId: {}", userId);
        return dtoConverter.toDTO(updatedUser);
    }
}
