package org.joker.comfypilot.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 * 自动填充创建时间、更新时间、创建人、更新人
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 自动填充创建人（从当前登录用户获取，暂时使用默认值）
        this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
        // 自动填充更新人
        this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新人
        this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }

    /**
     * 获取当前登录用户ID
     * 从 UserContextHolder 获取当前用户ID
     *
     * @return 用户ID，未登录时返回0
     */
    private Long getCurrentUserId() {
        Long userId = UserContextHolder.getCurrentUserId();
        // 如果未登录或获取失败，返回默认值0
        return userId != null ? userId : 0L;
    }
}
