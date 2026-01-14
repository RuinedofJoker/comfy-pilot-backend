package org.joker.comfypilot.common.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO 基类
 * 所有 DTO 对象都应继承此类
 */
@Data
public abstract class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
