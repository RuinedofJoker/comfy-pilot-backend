package org.joker.comfypilot.auth.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.auth.application.dto.*;
import org.joker.comfypilot.auth.application.service.AuthService;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证接口控制器
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户注册", description = "新用户注册账户")
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Validated @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "用户登录", description = "用户邮箱密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "用户登出", description = "用户登出，撤销当前Token")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        String token = extractToken(request);
        authService.logout(userId, token);
        return Result.success("登出成功", null);
    }

    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public Result<RefreshTokenResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return Result.success("刷新成功", response);
    }

    @Operation(summary = "请求密码重置", description = "发送密码重置邮件")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Result.success("重置邮件已发送", null);
    }

    @Operation(summary = "确认密码重置", description = "使用重置令牌设置新密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }

    /**
     * 从请求头提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
