package org.joker.comfypilot.session.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.repository.ChatSessionRepository;
import org.joker.comfypilot.session.infrastructure.persistence.converter.ChatSessionConverter;
import org.joker.comfypilot.session.infrastructure.persistence.mapper.ChatSessionMapper;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatSessionPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 会话仓储实现
 */
@Repository
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatSessionConverter chatSessionConverter;

    @Override
    public ChatSession save(ChatSession chatSession) {
        ChatSessionPO po = chatSessionConverter.toPO(chatSession);
        chatSessionMapper.insert(po);
        return chatSessionConverter.toDomain(po);
    }

    @Override
    public ChatSession updateById(Long id, ChatSession chatSession) {
        ChatSessionPO po = chatSessionConverter.toPO(chatSession);
        po.setId(id);
        chatSessionMapper.updateById(po);
        return chatSessionConverter.toDomain(po);
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        ChatSessionPO po = chatSessionMapper.selectById(id);
        return Optional.ofNullable(po).map(chatSessionConverter::toDomain);
    }

    @Override
    public Optional<ChatSession> findBySessionCode(String sessionCode) {
        LambdaQueryWrapper<ChatSessionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionPO::getSessionCode, sessionCode);
        ChatSessionPO po = chatSessionMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(chatSessionConverter::toDomain);
    }

    @Override
    public List<ChatSession> findByUserId(Long userId) {
        LambdaQueryWrapper<ChatSessionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionPO::getUserId, userId)
                .orderByDesc(ChatSessionPO::getCreateTime);
        return chatSessionMapper.selectList(wrapper).stream()
                .map(chatSessionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findActiveByUserIdAndComfyuiServerId(Long userId, Long comfyuiServerId) {
        LambdaQueryWrapper<ChatSessionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(ChatSessionPO::getUserId, userId)
                .eq(comfyuiServerId != null, ChatSessionPO::getComfyuiServerId, comfyuiServerId)
                .eq(ChatSessionPO::getStatus, "ACTIVE")
                .orderByDesc(ChatSessionPO::getCreateTime);
        return chatSessionMapper.selectList(wrapper).stream()
                .map(chatSessionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSession updateById(ChatSession chatSession) {
        ChatSessionPO po = chatSessionConverter.toPO(chatSession);
        chatSessionMapper.updateById(po);
        return chatSessionConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        chatSessionMapper.deleteById(id);
    }
}
