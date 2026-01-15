package org.joker.comfypilot.workflow.domain.repository;

import org.joker.comfypilot.workflow.domain.entity.Workflow;

import java.util.List;
import java.util.Optional;

/**
 * 工作流仓储接口
 */
public interface WorkflowRepository {

    /**
     * 保存工作流
     */
    Workflow save(Workflow workflow);

    /**
     * 根据ID查询工作流
     */
    Optional<Workflow> findById(Long id);

    /**
     * 根据用户ID查询工作流列表
     */
    List<Workflow> findByUserId(Long userId);

    /**
     * 根据服务ID查询工作流列表
     */
    List<Workflow> findByServiceId(Long serviceId);

    /**
     * 删除工作流
     */
    void deleteById(Long id);
}
