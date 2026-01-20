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

}
