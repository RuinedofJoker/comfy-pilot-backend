package org.joker.comfypilot.user.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.user.application.dto.UpdateUserRequest;
import org.joker.comfypilot.user.application.dto.UserDTO;
import org.joker.comfypilot.user.application.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户接口控制器
 */
@Tag(name = "用户管理", description = "用户信息管理接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        Long userId = UserContextHolder.getCurrentUserId();
        UserDTO userDTO = userService.getCurrentUser(userId);
        return Result.success(userDTO);
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的基本信息（用户名、头像等）")
    @PutMapping("/me")
    public Result<UserDTO> updateUser(@Validated @RequestBody UpdateUserRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        UserDTO userDTO = userService.updateUser(userId, request);
        return Result.success(userDTO);
    }
}
