package org.joker.comfypilot.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "注册响应")
public class RegisterResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "邮箱地址")
    private String email;
}
