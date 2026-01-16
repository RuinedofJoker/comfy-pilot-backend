package org.joker.comfypilot.workflow.application.converter;

import org.joker.comfypilot.workflow.application.dto.WorkflowVersionDTO;
import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;
import org.mapstruct.Mapper;

/**
 * 工作流版本Entity与DTO转换器
 */
@Mapper(componentModel = "spring")
public interface WorkflowVersionDTOConverter {

    /**
     * Entity转DTO
     *
     * @param entity 领域实体
     * @return DTO
     */
    WorkflowVersionDTO toDTO(WorkflowVersion entity);
}
