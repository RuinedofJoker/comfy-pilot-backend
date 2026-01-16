package org.joker.comfypilot.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowVersionPO;

/**
 * 工作流版本Mapper接口
 */
@Mapper
public interface WorkflowVersionMapper extends BaseMapper<WorkflowVersionPO> {

    /**
     * 获取工作流的最大版本号
     *
     * @param workflowId 工作流ID
     * @return 最大版本号（如果没有版本则返回null）
     */
    @Select("SELECT MAX(version_number) FROM workflow_version WHERE workflow_id = #{workflowId} AND is_deleted = 0")
    Integer getMaxVersionNumber(Long workflowId);
}
