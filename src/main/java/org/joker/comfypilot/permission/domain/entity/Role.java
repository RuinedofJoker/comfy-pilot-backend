package org.joker.comfypilot.permission.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * 角色领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码（唯一）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统内置角色
     */
    private Boolean isSystem;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 是否已删除
     */
    private Boolean isDeleted;

    /**
     * 更新角色名称
     */
    public void updateRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BusinessException("角色名称不能为空");
        }
        if (roleName.length() > 100) {
            throw new BusinessException("角色名称长度不能超过100个字符");
        }
        this.roleName = roleName;
    }

    /**
     * 更新角色描述
     */
    public void updateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new BusinessException("角色描述长度不能超过500个字符");
        }
        this.description = description;
    }

    /**
     * 检查是否可以删除
     */
    public boolean canDelete() {
        // 系统内置角色不可删除
        return !Boolean.TRUE.equals(this.isSystem);
    }
}
