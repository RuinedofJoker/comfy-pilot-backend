package org.joker.comfypilot.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新Token请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新Token请求")
public class RefreshTokenRequest {

    @Schema(description = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
