package org.joker.comfypilot.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新Token响应")
public class RefreshTokenResponse {

    @Schema(description = "新的访问令牌")
    private String accessToken;

    @Schema(description = "访问令牌过期时间（秒）")
    private Long expiresIn;
}
