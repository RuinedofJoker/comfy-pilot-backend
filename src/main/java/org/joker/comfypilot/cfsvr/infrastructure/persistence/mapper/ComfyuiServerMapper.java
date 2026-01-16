package org.joker.comfypilot.cfsvr.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.po.ComfyuiServerPO;

/**
 * ComfyUI服务Mapper接口
 */
@Mapper
public interface ComfyuiServerMapper extends BaseMapper<ComfyuiServerPO> {
}
