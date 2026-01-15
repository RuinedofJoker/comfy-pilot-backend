package org.joker.comfypilot.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowVersionPO;

import java.util.List;

/**
 * 工作流版本Mapper接口
 */
@Mapper
public interface WorkflowVersionMapper extends BaseMapper<WorkflowVersionPO> {

    /**
     * 根据工作流ID查询版本列表
     */
    List<WorkflowVersionPO> selectByWorkflowId(Long workflowId);

    /**
     * 根据会话ID查询版本列表
     */
    List<WorkflowVersionPO> selectBySessionId(Long sessionId);
}
