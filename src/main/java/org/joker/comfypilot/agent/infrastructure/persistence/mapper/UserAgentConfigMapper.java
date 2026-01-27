package org.joker.comfypilot.agent.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.UserAgentConfigPO;

/**
 * 用户Agent配置Mapper接口
 */
@Mapper
public interface UserAgentConfigMapper extends BaseMapper<UserAgentConfigPO> {
}
