package org.joker.comfypilot.workflow.infrastructure.persistence.converter;

import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowVersionPO;
import org.mapstruct.Mapper;

/**
 * 工作流版本PO与Entity转换器
 */
@Mapper(componentModel = "spring")
public interface WorkflowVersionConverter {

    /**
     * PO转领域实体
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    WorkflowVersion toDomain(WorkflowVersionPO po);

    /**
     * 领域实体转PO
     *
     * @param domain 领域实体
     * @return 持久化对象
     */
    WorkflowVersionPO toPO(WorkflowVersion domain);
}
