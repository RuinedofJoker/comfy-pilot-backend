package org.joker.comfypilot.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;
import org.joker.comfypilot.user.domain.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户信息")
public class UserDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "用户状态")
    private UserStatus status;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime lastLoginTime;
}
