package org.joker.comfypilot.user.application.dto;

import lombok.Data;

/**
 * 用户更新请求DTO
 */
@Data
public class UserUpdateRequest {

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatarUrl;
}
