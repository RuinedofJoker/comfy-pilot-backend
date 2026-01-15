package org.joker.comfypilot.workflow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流版本持久化对象
 */
@Data
@TableName("workflow_version")
public class WorkflowVersionPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 版本号
     */
    private Integer versionNumber;

    /**
     * 工作流快照
     */
    private String workflowData;

    /**
     * 变更摘要
     */
    private String changeSummary;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}
