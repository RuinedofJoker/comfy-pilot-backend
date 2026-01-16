package org.joker.comfypilot.auth.infrastructure.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserTokenRedis;
import org.joker.comfypilot.auth.infrastructure.redis.repository.SessionRedisRepository;
import org.joker.comfypilot.auth.infrastructure.redis.repository.TokenRedisRepository;
import org.joker.comfypilot.auth.infrastructure.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * 从请求头获取Token，验证并设置用户上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenRedisRepository tokenRedisRepository;
    private final SessionRedisRepository sessionRedisRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取Token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("请求未携带Token");
            return true;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败");
                return true;
            }

            // 从Redis查询Token信息
            UserTokenRedis tokenRedis = tokenRedisRepository.getAccessToken(token);
            if (tokenRedis == null) {
                log.warn("Token不存在于Redis");
                return true;
            }

            // 检查Token是否被撤销
            if (tokenRedis.getIsRevoked()) {
                log.warn("Token已被撤销");
                return true;
            }

            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 从Redis获取用户会话
            UserSessionRedis session = sessionRedisRepository.getSession(userId);
            if (session == null) {
                log.warn("用户会话不存在, userId: {}", userId);
                return true;
            }

            // 设置到ThreadLocal
            UserContextHolder.setUserSession(session);

            // 刷新会话过期时间
            sessionRedisRepository.refreshSession(userId);

            log.debug("用户认证成功, userId: {}", userId);
            return true;

        } catch (Exception e) {
            log.error("认证拦截器异常", e);
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除ThreadLocal，避免内存泄漏
        UserContextHolder.clear();
    }
}
