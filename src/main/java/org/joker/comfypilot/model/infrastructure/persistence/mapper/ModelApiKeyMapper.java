package org.joker.comfypilot.model.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelApiKeyPO;

/**
 * 模型API密钥Mapper接口
 */
@Mapper
public interface ModelApiKeyMapper extends BaseMapper<ModelApiKeyPO> {
}
