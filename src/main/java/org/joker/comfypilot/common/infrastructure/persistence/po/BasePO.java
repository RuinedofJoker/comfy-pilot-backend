package org.joker.comfypilot.common.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 持久化对象基类
 * 所有 PO 对象都应继承此类
 */
@Data
public abstract class BasePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
