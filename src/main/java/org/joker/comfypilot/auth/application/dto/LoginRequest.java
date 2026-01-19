package org.joker.comfypilot.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求")
public class LoginRequest {

    @Schema(description = "邮箱地址", example = "user@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "密码", example = "password123", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
