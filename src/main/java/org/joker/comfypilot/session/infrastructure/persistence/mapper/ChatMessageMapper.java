package org.joker.comfypilot.session.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatMessagePO;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessagePO> {

    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessagePO> selectBySessionId(Long sessionId);
}
