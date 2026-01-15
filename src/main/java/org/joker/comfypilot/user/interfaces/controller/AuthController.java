package org.joker.comfypilot.user.interfaces.controller;

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
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserApplicationService userApplicationService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody UserRegisterRequest request) {
        // TODO: 实现用户注册逻辑
        return null;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginRequest request) {
        // TODO: 实现用户登录逻辑，返回JWT token
        return null;
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 实现用户登出逻辑
        return null;
    }
}
