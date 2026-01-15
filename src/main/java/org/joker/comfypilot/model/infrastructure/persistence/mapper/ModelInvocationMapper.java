package org.joker.comfypilot.model.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelInvocationPO;

/**
 * 模型调用记录Mapper接口
 */
@Mapper
public interface ModelInvocationMapper extends BaseMapper<ModelInvocationPO> {
}
