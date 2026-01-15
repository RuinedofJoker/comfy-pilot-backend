package org.joker.comfypilot.user.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.user.application.dto.*;
import org.joker.comfypilot.user.application.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserApplicationService userApplicationService;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        // TODO: 实现获取当前用户信息逻辑
        return null;
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/me")
    public Result<UserDTO> updateUser(@RequestBody UserUpdateRequest request) {
        // TODO: 实现更新用户信息逻辑
        return null;
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        // TODO: 实现修改密码逻辑
        return null;
    }
}
