package org.joker.comfypilot.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Schema(description = "重置令牌", required = true)
    @NotBlank(message = "重置令牌不能为空")
    private String token;

    @Schema(description = "新密码（最小8位，包含字母和数字）", example = "newpassword123", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String newPassword;
}
