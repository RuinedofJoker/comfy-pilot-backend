package org.joker.comfypilot.auth.infrastructure.redis.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.auth.domain.enums.TokenType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户Token Redis模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenRedis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * JWT Token完整字符串
     */
    private String token;

    /**
     * Token类型
     */
    private TokenType tokenType;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime expiresAt;

    /**
     * 是否已撤销（黑名单标记）
     */
    private Boolean isRevoked;

    /**
     * 撤销时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime revokedAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createTime;
}
