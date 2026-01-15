package org.joker.comfypilot.workflow.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowPO;

import java.util.List;

/**
 * 工作流Mapper接口
 */
@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowPO> {

    /**
     * 根据用户ID查询工作流列表
     */
    List<WorkflowPO> selectByUserId(Long userId);

    /**
     * 根据服务ID查询工作流列表
     */
    List<WorkflowPO> selectByServiceId(Long serviceId);
}
