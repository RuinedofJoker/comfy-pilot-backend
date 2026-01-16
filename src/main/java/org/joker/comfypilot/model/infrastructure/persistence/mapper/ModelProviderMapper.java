package org.joker.comfypilot.model.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelProviderPO;

/**
 * 模型提供商Mapper接口
 */
@Mapper
public interface ModelProviderMapper extends BaseMapper<ModelProviderPO> {
}
