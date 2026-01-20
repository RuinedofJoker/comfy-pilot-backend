package org.joker.comfypilot.workflow.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工作流版本领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowVersion extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属工作流ID
     */
    private Long workflowId;

    /**
     * 版本号（UUID）
     */
    private String versionCode;

    /**
     * 来源版本号
     */
    private String fromVersionCode;

    /**
     * 版本内容（JSON格式）
     */
    private String content;

    /**
     * 内容的SHA-256哈希值
     */
    private String contentHash;

    /**
     * 变更摘要（Agent生成）
     */
    private String changeSummary;

    /**
     * 关联的会话ID（如果是Agent对话生成）
     */
    private Long sessionId;

    /**
     * 关联的会话消息ID（如果是Agent对话生成）
     */
    private Long messageId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    // ==================== BaseEntity抽象方法实现 ====================

    /**
     * 获取更新时间
     * 版本是只读的，一旦创建就不能修改，因此返回null
     *
     * @return null（版本不支持更新）
     */
    @Override
    public LocalDateTime getUpdateTime() {
        return null;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断内容是否相同
     *
     * @param otherVersion 另一个版本
     * @return 内容是否相同
     */
    public boolean isSameContent(WorkflowVersion otherVersion) {
        if (otherVersion == null) {
            return false;
        }
        // 先比较哈希值
        if (this.contentHash != null && otherVersion.contentHash != null) {
            if (!this.contentHash.equals(otherVersion.contentHash)) {
                return false;
            }
        }
        // 哈希值相同时，再比较完整内容
        if (this.content != null && otherVersion.content != null) {
            return this.content.equals(otherVersion.content);
        }
        return false;
    }
}
