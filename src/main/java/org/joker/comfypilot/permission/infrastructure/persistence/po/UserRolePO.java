package org.joker.comfypilot.permission.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.infrastructure.persistence.annotation.UniqueKey;

import java.time.LocalDateTime;

/**
 * 用户角色关联持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_role")
public class UserRolePO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（联合唯一索引）
     */
    @UniqueKey(group = "user_role", order = 1)
    private Long userId;

    /**
     * 角色ID（联合唯一索引）
     */
    @UniqueKey(group = "user_role", order = 2)
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
