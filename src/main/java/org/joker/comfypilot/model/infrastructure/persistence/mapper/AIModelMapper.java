package org.joker.comfypilot.model.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.AIModelPO;

/**
 * AI模型Mapper接口
 */
@Mapper
public interface AIModelMapper extends BaseMapper<AIModelPO> {
}
