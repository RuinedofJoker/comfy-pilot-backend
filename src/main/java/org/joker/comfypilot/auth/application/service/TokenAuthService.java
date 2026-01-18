package org.joker.comfypilot.auth.application.service;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserTokenRedis;
import org.joker.comfypilot.auth.infrastructure.redis.repository.SessionRedisRepository;
import org.joker.comfypilot.auth.infrastructure.redis.repository.TokenRedisRepository;
import org.joker.comfypilot.auth.infrastructure.util.JwtUtil;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Token认证服务
 * 提供统一的Token验证逻辑，供REST API和WebSocket使用
 */
@Slf4j
@Service
public class TokenAuthService {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenRedisRepository tokenRedisRepository;
    @Autowired
    private SessionRedisRepository sessionRedisRepository;

    /**
     * 验证Token并返回用户会话
     *
     * @param token Token字符串（可能包含Bearer前缀）
     * @return 用户会话，验证失败返回null
     */
    public UserSessionRedis validateTokenAndGetSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("Token为空");
            return null;
        }

        // 移除Bearer前缀（如果存在）
        if (token.startsWith(AuthConstants.BEARER_PREFIX)) {
            token = token.substring(AuthConstants.BEARER_PREFIX.length());
        }

        try {
            // 1. 验证Token格式和签名
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败");
                return null;
            }

            // 2. 从Redis查询Token信息
            UserTokenRedis tokenRedis = tokenRedisRepository.getAccessToken(token);
            if (tokenRedis == null) {
                log.warn("Token不存在于Redis");
                return null;
            }

            // 3. 检查Token是否被撤销
            if (tokenRedis.getIsRevoked()) {
                log.warn("Token已被撤销");
                return null;
            }

            // 4. 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 5. 从Redis获取用户会话
            UserSessionRedis session = sessionRedisRepository.getSession(userId);
            if (session == null) {
                log.warn("用户会话不存在, userId: {}", userId);
                return null;
            }

            // 6. 刷新会话过期时间
            sessionRedisRepository.refreshSession(userId);

            log.debug("Token验证成功, userId: {}", userId);
            return session;

        } catch (Exception e) {
            log.error("Token验证异常", e);
            return null;
        }
    }
}
