package org.joker.comfypilot.agent.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentExecutionPO;

/**
 * Agent执行记录Mapper接口
 */
@Mapper
public interface AgentExecutionMapper extends BaseMapper<AgentExecutionPO> {
}
