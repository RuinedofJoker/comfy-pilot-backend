package org.joker.comfypilot.session.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.session.domain.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会话领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话编码（唯一标识）
     */
    private String sessionCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * ComfyUI服务ID
     */
    private Long comfyuiServerId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话使用的agent的agentCode
     */
    private String agentCode;

    /**
     * 会话使用的agent的运行时配置（json格式）
     */
    private Map<String, Object> agentConfig;

    /**
     * 会话状态
     */
    private SessionStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 领域行为：归档会话
     */
    public void archive() {
        this.status = SessionStatus.ARCHIVED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：更新标题
     */
    public void updateTitle(String newTitle) {
        this.title = newTitle;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：检查会话是否活跃
     */
    public boolean isActive() {
        return SessionStatus.ACTIVE.equals(this.status);
    }
}
