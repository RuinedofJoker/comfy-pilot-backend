package org.joker.comfypilot.session.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatSessionPO;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionPO> {

    /**
     * 根据用户ID查询会话列表
     */
    List<ChatSessionPO> selectByUserId(Long userId);

    /**
     * 根据工作流ID查询会话列表
     */
    List<ChatSessionPO> selectByWorkflowId(Long workflowId);
}
