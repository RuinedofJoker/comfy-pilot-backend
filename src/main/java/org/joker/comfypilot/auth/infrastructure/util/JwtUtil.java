package org.joker.comfypilot.auth.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:comfy-pilot-secret-key-for-jwt-token-generation-2024}")
    private String secret;

    @Value("${jwt.access-token-expiration:86400000}")
    private Long accessTokenExpiration; // 24小时

    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration; // 7天

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String generateAccessToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "ACCESS");
        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");
        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * 生成Token
     *
     * @param claims     声明
     * @param expiration 过期时间(毫秒)
     * @return JWT Token
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析Token
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取Token过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
}
