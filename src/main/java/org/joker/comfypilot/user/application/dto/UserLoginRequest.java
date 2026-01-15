package org.joker.comfypilot.user.application.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class UserLoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
