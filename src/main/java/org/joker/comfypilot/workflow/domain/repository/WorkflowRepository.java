package org.joker.comfypilot.workflow.domain.repository;

import org.joker.comfypilot.workflow.domain.entity.Workflow;

import java.util.List;
import java.util.Optional;

/**
 * 工作流仓储接口
 */
public interface WorkflowRepository {

    /**
     * 根据ID查询
     *
     * @param id 工作流ID
     * @return 工作流实体
     */
    Optional<Workflow> findById(Long id);

    /**
     * 查询所有工作流
     *
     * @return 工作流列表
     */
    List<Workflow> findAll();

    /**
     * 根据ComfyUI服务ID查询
     *
     * @param comfyuiServerId ComfyUI服务ID
     * @return 工作流列表
     */
    List<Workflow> findByComfyuiServerId(Long comfyuiServerId);

    /**
     * 根据创建人查询
     *
     * @param createBy 创建人ID
     * @return 工作流列表
     */
    List<Workflow> findByCreateBy(Long createBy);

    /**
     * 保存工作流
     *
     * @param workflow 工作流实体
     * @return 保存后的工作流实体
     */
    Workflow save(Workflow workflow);

    /**
     * 根据ID删除
     *
     * @param id 工作流ID
     */
    void deleteById(Long id);
}
