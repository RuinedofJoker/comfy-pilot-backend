package org.joker.comfypilot.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域实体基类
 * 所有领域实体都应继承此类
 *
 * @param <ID> 主键类型
 */
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 获取实体ID
     *
     * @return 实体ID
     */
    public abstract ID getId();

    /**
     * 设置实体ID
     *
     * @param id 实体ID
     */
    public abstract void setId(ID id);

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public abstract LocalDateTime getCreateTime();

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public abstract LocalDateTime getUpdateTime();

    /**
     * 判断是否为新实体（未持久化）
     *
     * @return true-新实体，false-已持久化
     */
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity<?> that = (BaseEntity<?>) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getId() + "}";
    }
}
