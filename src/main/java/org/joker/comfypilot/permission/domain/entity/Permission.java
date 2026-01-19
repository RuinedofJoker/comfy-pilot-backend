package org.joker.comfypilot.permission.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限编码（唯一，格式：资源:操作，如 workflow:create）
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 资源类型（如 workflow、user）
     */
    private String resourceType;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
