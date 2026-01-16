package org.joker.comfypilot.auth.application.service;

import jakarta.servlet.http.HttpServletRequest;
import org.joker.comfypilot.auth.application.dto.*;

/**
 * 认证应用服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID和邮箱
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求对象（用于获取客户端IP）
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @param token  访问令牌
     */
    void logout(Long userId, String token);

    /**
     * 刷新Token
     *
     * @param request 刷新Token请求
     * @return 新的访问令牌
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * 请求密码重置
     *
     * @param request 忘记密码请求
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * 确认密码重置
     *
     * @param request 重置密码请求
     */
    void resetPassword(ResetPasswordRequest request);
}
