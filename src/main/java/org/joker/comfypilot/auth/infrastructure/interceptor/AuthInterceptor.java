package org.joker.comfypilot.auth.infrastructure.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserTokenRedis;
import org.joker.comfypilot.auth.infrastructure.redis.repository.SessionRedisRepository;
import org.joker.comfypilot.auth.infrastructure.redis.repository.TokenRedisRepository;
import org.joker.comfypilot.auth.infrastructure.util.JwtUtil;
import org.joker.comfypilot.common.constant.AuthConstants;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取Token
        String authHeader = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            log.debug("请求未携带Token");
            return true; // 没有Token，放行（由权限注解控制是否需要认证）
        }

        String token = authHeader.substring(AuthConstants.BEARER_PREFIX.length());

        try {
            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token验证失败\"}");
                return false; // 拦截请求
            }

            // 从Redis查询Token信息
            UserTokenRedis tokenRedis = tokenRedisRepository.getAccessToken(token);
            if (tokenRedis == null) {
                log.warn("Token不存在于Redis");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token不存在或已过期\"}");
                return false; // 拦截请求
            }

            // 检查Token是否被撤销
            if (tokenRedis.getIsRevoked()) {
                log.warn("Token已被撤销");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已被撤销\"}");
                return false; // 拦截请求
            }

            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 从Redis获取用户会话
            UserSessionRedis session = sessionRedisRepository.getSession(userId);
            if (session == null) {
                log.warn("用户会话不存在, userId: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户会话不存在，请重新登录\"}");
                return false; // 拦截请求
            }

            // 设置到ThreadLocal
            UserContextHolder.setUserSession(session);

            // 刷新会话过期时间
            sessionRedisRepository.refreshSession(userId);

            log.debug("用户认证成功, userId: {}", userId);
            return true; // 认证成功，放行

        } catch (Exception e) {
            log.error("认证拦截器异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"认证服务异常\"}");
            return false; // 异常情况也应该拦截
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除ThreadLocal，避免内存泄漏
        UserContextHolder.clear();
    }
}
