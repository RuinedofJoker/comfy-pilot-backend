package org.joker.comfypilot.user.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.user.application.dto.UserDTO;
import org.joker.comfypilot.user.application.dto.UserLoginRequest;
import org.joker.comfypilot.user.application.dto.UserRegisterRequest;
import org.joker.comfypilot.user.application.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "用户注册、登录、登出相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserApplicationService userApplicationService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody UserRegisterRequest request) {
        // TODO: 实现用户注册逻辑
        return null;
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录并返回JWT Token")
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginRequest request) {
        // TODO: 实现用户登录逻辑，返回JWT token
        return null;
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户登出并使Token失效")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 实现用户登出逻辑
        return null;
    }
}
