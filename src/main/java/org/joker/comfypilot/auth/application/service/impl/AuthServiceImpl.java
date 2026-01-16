package org.joker.comfypilot.auth.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.application.dto.*;
import org.joker.comfypilot.auth.application.service.AuthService;
import org.joker.comfypilot.auth.domain.enums.TokenType;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserTokenRedis;
import org.joker.comfypilot.auth.infrastructure.redis.repository.SessionRedisRepository;
import org.joker.comfypilot.auth.infrastructure.redis.repository.TokenRedisRepository;
import org.joker.comfypilot.auth.infrastructure.util.JwtUtil;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.UnauthorizedException;
import org.joker.comfypilot.user.application.dto.UserDTO;
import org.joker.comfypilot.user.domain.entity.User;
import org.joker.comfypilot.user.domain.enums.UserStatus;
import org.joker.comfypilot.user.domain.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 认证应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRedisRepository tokenRedisRepository;
    private final SessionRedisRepository sessionRedisRepository;
    private final org.joker.comfypilot.permission.application.service.PermissionService permissionService;
    private final org.joker.comfypilot.auth.infrastructure.redis.repository.PasswordResetTokenRedisRepository passwordResetTokenRedisRepository;
    private final org.joker.comfypilot.notification.application.service.EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request) {
        log.info("用户注册, email: {}", request.getEmail());

        // 验证邮箱唯一性
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 生成用户编码
        String userCode = generateUserCode();

        // 加密密码
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 创建用户实体
        User user = User.builder()
                .userCode(userCode)
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .build();

        // 保存用户
        User savedUser = userRepository.save(user);

        // 为新用户分配默认角色 USER
        try {
            permissionService.assignRoleToUser(savedUser.getId(), "USER");
            log.info("为新用户分配USER角色成功, userId: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("为新用户分配角色失败, userId: {}, error: {}", savedUser.getId(), e.getMessage());
            // 不影响注册流程，继续执行
        }

        log.info("用户注册成功, userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("用户登录, email: {}", request.getEmail());

        // 查询用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("邮箱或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("邮箱或密码错误");
        }

        // 检查用户状态
        if (!user.canLogin()) {
            throw new BusinessException("用户状态异常，无法登录");
        }

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 保存Token到Redis
        saveTokenToRedis(user.getId(), accessToken, TokenType.ACCESS);
        saveTokenToRedis(user.getId(), refreshToken, TokenType.REFRESH);

        // 创建用户会话
        createUserSession(user);

        // 获取客户端真实IP并更新最后登录信息
        String clientIp = org.joker.comfypilot.common.util.IpUtil.getClientIp(httpRequest);
        user.updateLastLogin(LocalDateTime.now(), clientIp);
        userRepository.save(user);

        log.info("用户登录成功, userId: {}", user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .user(convertToUserDTO(user))
                .build();
    }

    @Override
    public void logout(Long userId, String token) {
        log.info("用户登出, userId: {}", userId);

        // 撤销Token
        tokenRedisRepository.revokeToken(token, TokenType.ACCESS);

        // 删除用户会话
        sessionRedisRepository.deleteSession(userId);

        log.info("用户登出成功, userId: {}", userId);
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("刷新Token");

        String refreshToken = request.getRefreshToken();

        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("刷新令牌无效");
        }

        // 从Redis检查刷新令牌
        UserTokenRedis tokenRedis = tokenRedisRepository.getRefreshToken(refreshToken);
        if (tokenRedis == null) {
            throw new UnauthorizedException("刷新令牌不存在");
        }

        if (tokenRedis.getIsRevoked()) {
            throw new UnauthorizedException("刷新令牌已被撤销");
        }

        // 生成新的访问令牌
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        // 保存新Token到Redis
        saveTokenToRedis(userId, newAccessToken, TokenType.ACCESS);

        log.info("Token刷新成功, userId: {}", userId);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(86400L)
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("请求密码重置, email: {}", request.getEmail());

        // 验证邮箱是否存在
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("邮箱不存在"));

        // 生成UUID重置令牌
        String resetToken = UUID.randomUUID().toString();

        // 保存到Redis (TTL 15分钟)
        org.joker.comfypilot.auth.infrastructure.redis.model.PasswordResetTokenRedis tokenRedis =
                org.joker.comfypilot.auth.infrastructure.redis.model.PasswordResetTokenRedis.builder()
                        .userId(user.getId())
                        .token(resetToken)
                        .expiresAt(LocalDateTime.now().plusMinutes(15))
                        .isUsed(false)
                        .createTime(LocalDateTime.now())
                        .build();
        passwordResetTokenRedisRepository.saveResetToken(tokenRedis);

        // 调用通知模块发送重置邮件
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("密码重置邮件已发送, email: {}", request.getEmail());
        } catch (Exception e) {
            log.error("发送密码重置邮件失败, email: {}, error: {}", request.getEmail(), e.getMessage());
            throw new BusinessException("发送密码重置邮件失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        log.info("确认密码重置, token: {}", request.getToken());

        // 从Redis验证重置令牌有效性
        org.joker.comfypilot.auth.infrastructure.redis.model.PasswordResetTokenRedis tokenRedis =
                passwordResetTokenRedisRepository.getResetToken(request.getToken());

        if (tokenRedis == null) {
            throw new BusinessException("重置令牌无效或已过期");
        }

        // 检查令牌是否已使用
        if (tokenRedis.getIsUsed()) {
            throw new BusinessException("重置令牌已被使用");
        }

        // 检查令牌是否过期
        if (LocalDateTime.now().isAfter(tokenRedis.getExpiresAt())) {
            throw new BusinessException("重置令牌已过期");
        }

        // 获取用户ID
        Long userId = tokenRedis.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 使用BCrypt加密新密码
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());

        // 更新用户密码
        user.updatePassword(newPasswordHash);
        userRepository.save(user);

        // 在Redis中标记令牌为已使用
        passwordResetTokenRedisRepository.markTokenAsUsed(request.getToken(), LocalDateTime.now());

        // 撤销该用户所有现有Token（强制重新登录）
        revokeAllUserTokens(userId);

        log.info("密码重置成功, userId: {}", userId);
    }

    /**
     * 生成用户编码
     */
    private String generateUserCode() {
        return "USER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 保存Token到Redis
     */
    private void saveTokenToRedis(Long userId, String token, TokenType tokenType) {
        Date expirationDate = jwtUtil.getExpirationFromToken(token);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneOffset.UTC);

        UserTokenRedis tokenRedis = UserTokenRedis.builder()
                .userId(userId)
                .token(token)
                .tokenType(tokenType)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .createTime(LocalDateTime.now())
                .build();

        if (tokenType == TokenType.ACCESS) {
            tokenRedisRepository.saveAccessToken(tokenRedis);
        } else {
            tokenRedisRepository.saveRefreshToken(tokenRedis);
        }
    }

    /**
     * 创建用户会话
     */
    private void createUserSession(User user) {
        // 从权限模块获取用户的角色和权限
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        try {
            roles = permissionService.getUserRoles(user.getId()).stream()
                    .map(org.joker.comfypilot.permission.application.dto.RoleDTO::getRoleCode)
                    .collect(java.util.stream.Collectors.toList());
            permissions = permissionService.getUserPermissions(user.getId());
            log.info("获取用户权限信息成功, userId: {}, roles: {}, permissions: {}",
                    user.getId(), roles, permissions);
        } catch (Exception e) {
            log.error("获取用户权限信息失败, userId: {}, error: {}", user.getId(), e.getMessage());
            // 使用空列表，不影响登录流程
        }

        UserSessionRedis session = UserSessionRedis.builder()
                .userId(user.getId())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roles)
                .permissions(permissions)
                .lastAccessTime(LocalDateTime.now())
                .build();

        sessionRedisRepository.saveSession(session);
    }

    /**
     * 撤销用户所有Token
     */
    private void revokeAllUserTokens(Long userId) {
        tokenRedisRepository.revokeAllUserTokens(userId);
        log.info("已撤销用户所有Token, userId: {}", userId);
    }

    /**
     * 转换为UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
