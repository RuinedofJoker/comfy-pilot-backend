package org.joker.comfypilot.permission.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.annotation.UniqueKey;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 角色持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class RolePO extends BasePO {

    /**
     * 角色编码（唯一）
     */
    @UniqueKey
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
}
