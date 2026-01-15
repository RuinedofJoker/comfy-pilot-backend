package org.joker.comfypilot.user.application.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 邮箱
     */
    private String email;
}
