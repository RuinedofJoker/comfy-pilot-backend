package org.joker.comfypilot.user.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 用户DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {

    /**
     * 用户名
     */
    private String username;

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

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private String status;

    /**
     * 工作流数量
     */
    private Integer workflowCount;

    /**
     * 会话数量
     */
    private Integer sessionCount;
}
