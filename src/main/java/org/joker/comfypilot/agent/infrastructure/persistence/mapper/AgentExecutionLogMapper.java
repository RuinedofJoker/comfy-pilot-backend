package org.joker.comfypilot.agent.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentExecutionLogPO;

/**
 * Agent执行日志 Mapper 接口
 */
@Mapper
public interface AgentExecutionLogMapper extends BaseMapper<AgentExecutionLogPO> {
}
