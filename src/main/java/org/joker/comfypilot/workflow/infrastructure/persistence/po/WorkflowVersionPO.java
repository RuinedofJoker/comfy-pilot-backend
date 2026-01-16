package org.joker.comfypilot.workflow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 工作流版本持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("workflow_version")
public class WorkflowVersionPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 所属工作流ID
     */
    private Long workflowId;

    /**
     * 版本号（从1开始递增）
     */
    private Integer versionNumber;

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
}
