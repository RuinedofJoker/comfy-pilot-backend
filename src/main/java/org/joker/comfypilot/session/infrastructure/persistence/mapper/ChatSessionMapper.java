package org.joker.comfypilot.session.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatSessionPO;

/**
 * 会话 Mapper 接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionPO> {
}
