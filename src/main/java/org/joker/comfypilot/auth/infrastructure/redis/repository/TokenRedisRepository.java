package org.joker.comfypilot.auth.infrastructure.redis.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.domain.enums.TokenType;
import org.joker.comfypilot.auth.infrastructure.redis.model.UserTokenRedis;
import org.joker.comfypilot.common.util.RedisUtil;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Token Redis仓储
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    private static final String ACCESS_TOKEN_PREFIX = "auth:access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh_token:";
    private static final String USER_TOKENS_PREFIX = "auth:user_tokens:";

    private static final long ACCESS_TOKEN_TTL = 86400; // 24小时
    private static final long REFRESH_TOKEN_TTL = 604800; // 7天

    /**
     * 保存访问令牌
     */
    public void saveAccessToken(UserTokenRedis tokenRedis) {
        String key = ACCESS_TOKEN_PREFIX + tokenRedis.getToken();
        saveToken(key, tokenRedis, ACCESS_TOKEN_TTL);
        addToUserTokens(tokenRedis.getUserId(), tokenRedis.getToken());
    }

    /**
     * 保存刷新令牌
     */
    public void saveRefreshToken(UserTokenRedis tokenRedis) {
        String key = REFRESH_TOKEN_PREFIX + tokenRedis.getToken();
        saveToken(key, tokenRedis, REFRESH_TOKEN_TTL);
        addToUserTokens(tokenRedis.getUserId(), tokenRedis.getToken());
    }

    /**
     * 获取访问令牌
     */
    public UserTokenRedis getAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        return getToken(key);
    }

    /**
     * 获取刷新令牌
     */
    public UserTokenRedis getRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return getToken(key);
    }

    /**
     * 撤销令牌
     */
    public void revokeToken(String token, TokenType tokenType) {
        String key = tokenType == TokenType.ACCESS ?
                ACCESS_TOKEN_PREFIX + token : REFRESH_TOKEN_PREFIX + token;

        UserTokenRedis tokenRedis = getToken(key);
        if (tokenRedis != null) {
            tokenRedis.setIsRevoked(true);
            tokenRedis.setRevokedAt(LocalDateTime.now());
            saveToken(key, tokenRedis, tokenType == TokenType.ACCESS ? ACCESS_TOKEN_TTL : REFRESH_TOKEN_TTL);
        }
    }

    /**
     * 保存Token
     */
    private void saveToken(String key, UserTokenRedis tokenRedis, long ttl) {
        try {
            String json = objectMapper.writeValueAsString(tokenRedis);
            redisUtil.set(key, json, ttl, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("保存Token失败", e);
            throw new RuntimeException("保存Token失败", e);
        }
    }

    /**
     * 获取Token
     */
    private UserTokenRedis getToken(String key) {
        try {
            String json = (String) redisUtil.get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, UserTokenRedis.class);
        } catch (JsonProcessingException e) {
            log.error("获取Token失败", e);
            return null;
        }
    }

    /**
     * 添加到用户Token列表
     */
    private void addToUserTokens(Long userId, String token) {
        String key = USER_TOKENS_PREFIX + userId;
        redisUtil.sSet(key, token);
    }

    /**
     * 撤销用户所有Token
     */
    public void revokeAllUserTokens(Long userId) {
        String key = USER_TOKENS_PREFIX + userId;
        Object tokensObj = redisUtil.sGet(key);

        if (tokensObj instanceof java.util.Set) {
            @SuppressWarnings("unchecked")
            java.util.Set<Object> tokens = (java.util.Set<Object>) tokensObj;

            for (Object tokenObj : tokens) {
                String token = tokenObj.toString();
                // 尝试撤销访问令牌
                revokeToken(token, TokenType.ACCESS);
                // 尝试撤销刷新令牌
                revokeToken(token, TokenType.REFRESH);
            }

            // 清空用户Token列表
            redisUtil.del(key);
            log.info("已撤销用户所有Token, userId: {}", userId);
        }
    }
}
