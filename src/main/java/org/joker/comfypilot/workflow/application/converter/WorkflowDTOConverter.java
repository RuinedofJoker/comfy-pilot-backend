package org.joker.comfypilot.workflow.application.converter;

import org.joker.comfypilot.workflow.application.dto.WorkflowDTO;
import org.joker.comfypilot.workflow.domain.entity.Workflow;
import org.mapstruct.Mapper;

/**
 * 工作流Entity与DTO转换器
 */
@Mapper(componentModel = "spring")
public interface WorkflowDTOConverter {

    /**
     * Entity转DTO
     *
     * @param entity 领域实体
     * @return DTO
     */
    WorkflowDTO toDTO(Workflow entity);
}
