package org.joker.comfypilot.agent.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentConfigPO;

/**
 * Agent配置 Mapper 接口
 */
@Mapper
public interface AgentConfigMapper extends BaseMapper<AgentConfigPO> {
}
