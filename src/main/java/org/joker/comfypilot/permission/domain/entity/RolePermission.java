package org.joker.comfypilot.permission.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色权限关联领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
