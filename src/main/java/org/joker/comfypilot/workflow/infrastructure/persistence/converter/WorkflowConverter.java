package org.joker.comfypilot.workflow.infrastructure.persistence.converter;

import org.joker.comfypilot.workflow.domain.entity.Workflow;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowPO;
import org.mapstruct.Mapper;

/**
 * 工作流PO与Entity转换器
 */
@Mapper(componentModel = "spring")
public interface WorkflowConverter {

    /**
     * PO转领域实体
     * 注意：lockedByMessageId字段不从数据库加载，由Redis管理
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    Workflow toDomain(WorkflowPO po);

    /**
     * 领域实体转PO
     * 注意：忽略lockedByMessageId字段，该字段不存储到数据库
     *
     * @param domain 领域实体
     * @return 持久化对象
     */
    WorkflowPO toPO(Workflow domain);
}
