package org.joker.comfypilot.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowPO;

/**
 * 工作流Mapper接口
 */
@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowPO> {
}
