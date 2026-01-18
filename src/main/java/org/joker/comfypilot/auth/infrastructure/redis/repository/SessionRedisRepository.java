package org.joker.comfypilot.auth.infrastructure.redis.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;
import org.joker.comfypilot.common.util.RedisUtil;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Session Redis仓储
 */
@Slf4j
@Repository
public class SessionRedisRepository {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "auth:session:";
    private static final long SESSION_TTL = 86400; // 24小时

    /**
     * 保存用户会话
     */
    public void saveSession(UserSessionRedis session) {
        String key = SESSION_PREFIX + session.getUserId();
        try {
            session.setLastAccessTime(LocalDateTime.now());
            String json = objectMapper.writeValueAsString(session);
            redisUtil.set(key, json, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("保存用户会话成功, userId: {}", session.getUserId());
        } catch (JsonProcessingException e) {
            log.error("保存用户会话失败", e);
            throw new RuntimeException("保存用户会话失败", e);
        }
    }

    /**
     * 获取用户会话
     */
    public UserSessionRedis getSession(Long userId) {
        String key = SESSION_PREFIX + userId;
        try {
            String json = (String) redisUtil.get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, UserSessionRedis.class);
        } catch (JsonProcessingException e) {
            log.error("获取用户会话失败", e);
            return null;
        }
    }

    /**
     * 删除用户会话
     */
    public void deleteSession(Long userId) {
        String key = SESSION_PREFIX + userId;
        redisUtil.del(key);
        log.debug("删除用户会话成功, userId: {}", userId);
    }

    /**
     * 刷新会话过期时间
     */
    public void refreshSession(Long userId) {
        String key = SESSION_PREFIX + userId;
        redisUtil.expire(key, SESSION_TTL, TimeUnit.SECONDS);
    }
}
