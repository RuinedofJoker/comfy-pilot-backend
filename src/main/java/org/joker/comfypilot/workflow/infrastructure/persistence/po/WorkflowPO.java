package org.joker.comfypilot.workflow.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * 工作流持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workflow")
public class WorkflowPO extends BasePO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 服务ID
     */
    private Long serviceId;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 工作流JSON数据
     */
    private String workflowData;

    /**
     * 是否收藏
     */
    private Boolean isFavorite;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
}
