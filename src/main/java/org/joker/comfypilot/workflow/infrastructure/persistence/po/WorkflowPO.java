package org.joker.comfypilot.workflow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * 工作流持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("workflow")
public class WorkflowPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 所属ComfyUI服务ID
     */
    private Long comfyuiServerId;

    /**
     * 所属ComfyUI服务唯一标识符
     */
    private String comfyuiServerKey;

    /**
     * 当前激活版本的内容（JSON格式）
     */
    private String activeContent;

    /**
     * 激活内容的SHA-256哈希值
     */
    private String activeContentHash;

    /**
     * 工作流缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 锁定人ID
     */
    private Long lockedBy;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedAt;
}
