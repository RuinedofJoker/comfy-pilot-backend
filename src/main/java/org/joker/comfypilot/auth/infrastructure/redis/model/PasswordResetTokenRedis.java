package org.joker.comfypilot.auth.infrastructure.redis.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密码重置令牌 Redis模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenRedis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 重置令牌（UUID格式）
     */
    private String token;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime expiresAt;

    /**
     * 是否已使用
     */
    private Boolean isUsed;

    /**
     * 使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime usedAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createTime;
}
