package org.joker.comfypilot.session.infrastructure.websocket;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.application.service.TokenAuthService;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * WebSocket握手拦截器
 * 在WebSocket连接建立前进行Token认证
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private TokenAuthService tokenAuthService;
    @Autowired
    private ChatSessionService chatSessionService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        log.debug("WebSocket握手开始: {}", request.getURI());

        // 1. 从请求头获取Token
        List<String> authHeaders = request.getHeaders().get(AuthConstants.AUTHORIZATION_HEADER);
        String token = null;

        if (authHeaders != null && !authHeaders.isEmpty()) {
            token = authHeaders.getFirst();
        }

        // 2. 如果请求头没有，尝试从查询参数获取（兼容某些WebSocket客户端）
        if (token == null || token.trim().isEmpty()) {
            String query = request.getURI().getQuery();
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6); // "token=".length()
                        break;
                    }
                }
            }
        }

        // 3. 验证Token
        UserSessionRedis session = tokenAuthService.validateTokenAndGetSession(token);

        if (session == null) {
            log.warn("WebSocket认证失败: Token无效或已过期");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false; // 拒绝握手
        }

        // 4. 将用户ID存储到WebSocket会话属性中
        attributes.put(AuthConstants.USER_ID_ATTRIBUTE, session.getUserId());

        log.info("WebSocket认证成功: userId={}", session.getUserId());

        String sessionCode = String.valueOf(request.getAttributes().get(AuthConstants.SESSION_CODE_ATTRIBUTE));
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(sessionCode);
        if (!chatSessionDTO.getUserId().equals(session.getUserId())) {
            log.warn("WebSocket认证失败: 连接sessionCode不属于用户, userId={}, sessionCode={}", session.getUserId(), sessionCode);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false; // 拒绝握手
        }

        // 5. 将sessionCode存储到WebSocket会话属性中
        attributes.put(AuthConstants.SESSION_CODE_ATTRIBUTE, sessionCode);

        return true; // 允许握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常", exception);
        }
    }
}
