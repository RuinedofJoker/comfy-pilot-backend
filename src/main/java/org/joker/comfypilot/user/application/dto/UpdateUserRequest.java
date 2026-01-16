package org.joker.comfypilot.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户信息请求")
public class UpdateUserRequest {

    @Schema(description = "用户名", example = "张三")
    @Size(min = 1, max = 50, message = "用户名长度必须在1-50个字符之间")
    private String username;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
