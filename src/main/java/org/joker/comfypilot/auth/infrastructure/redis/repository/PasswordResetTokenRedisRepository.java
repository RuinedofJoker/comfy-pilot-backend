package org.joker.comfypilot.auth.infrastructure.redis.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.redis.model.PasswordResetTokenRedis;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.RedisUtil;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * 密码重置令牌 Redis仓储
 */
@Slf4j
@Repository
public class PasswordResetTokenRedisRepository {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String RESET_TOKEN_PREFIX = "auth:reset_token:";
    private static final long RESET_TOKEN_TTL = 900; // 15分钟

    /**
     * 保存密码重置令牌
     *
     * @param tokenRedis 重置令牌对象
     */
    public void saveResetToken(PasswordResetTokenRedis tokenRedis) {
        String key = RESET_TOKEN_PREFIX + tokenRedis.getToken();
        try {
            String json = objectMapper.writeValueAsString(tokenRedis);
            redisUtil.set(key, json, RESET_TOKEN_TTL, TimeUnit.SECONDS);
            log.info("保存密码重置令牌成功, userId: {}, token: {}", tokenRedis.getUserId(), tokenRedis.getToken());
        } catch (JsonProcessingException e) {
            log.error("保存密码重置令牌失败", e);
            throw new BusinessException("保存密码重置令牌失败", e);
        }
    }

    /**
     * 获取密码重置令牌
     *
     * @param token 令牌字符串
     * @return 重置令牌对象
     */
    public PasswordResetTokenRedis getResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        try {
            String json = (String) redisUtil.get(key);
            if (json == null) {
                log.warn("密码重置令牌不存在或已过期, token: {}", token);
                return null;
            }
            return objectMapper.readValue(json, PasswordResetTokenRedis.class);
        } catch (JsonProcessingException e) {
            log.error("获取密码重置令牌失败", e);
            return null;
        }
    }

    /**
     * 标记令牌为已使用
     *
     * @param token 令牌字符串
     */
    public void markTokenAsUsed(String token, java.time.LocalDateTime usedAt) {
        String key = RESET_TOKEN_PREFIX + token;
        PasswordResetTokenRedis tokenRedis = getResetToken(token);
        if (tokenRedis != null) {
            tokenRedis.setIsUsed(true);
            tokenRedis.setUsedAt(usedAt);
            try {
                String json = objectMapper.writeValueAsString(tokenRedis);
                // 更新后保留较短的TTL，避免占用空间
                redisUtil.set(key, json, 300, TimeUnit.SECONDS); // 5分钟
                log.info("标记密码重置令牌为已使用, token: {}", token);
            } catch (JsonProcessingException e) {
                log.error("标记令牌为已使用失败", e);
            }
        }
    }

    /**
     * 删除密码重置令牌
     *
     * @param token 令牌字符串
     */
    public void deleteResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        redisUtil.del(key);
        log.info("删除密码重置令牌, token: {}", token);
    }
}
