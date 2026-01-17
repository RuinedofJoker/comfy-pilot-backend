package org.joker.comfypilot.session.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.infrastructure.persistence.converter.ChatMessageConverter;
import org.joker.comfypilot.session.infrastructure.persistence.mapper.ChatMessageMapper;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatMessagePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageConverter chatMessageConverter;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        ChatMessagePO po = chatMessageConverter.toPO(chatMessage);
        chatMessageMapper.insert(po);
        return chatMessageConverter.toDomain(po);
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        ChatMessagePO po = chatMessageMapper.selectById(id);
        return Optional.ofNullable(po).map(chatMessageConverter::toDomain);
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatMessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessagePO::getSessionId, sessionId)
                .eq(ChatMessagePO::getIsDeleted, 0L)
                .orderByAsc(ChatMessagePO::getCreateTime);
        return chatMessageMapper.selectList(wrapper).stream()
                .map(chatMessageConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> findBySessionIdWithPagination(Long sessionId, int offset, int limit) {
        Page<ChatMessagePO> page = new Page<>(offset / limit + 1, limit);
        LambdaQueryWrapper<ChatMessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessagePO::getSessionId, sessionId)
                .eq(ChatMessagePO::getIsDeleted, 0L)
                .orderByAsc(ChatMessagePO::getCreateTime);
        Page<ChatMessagePO> result = chatMessageMapper.selectPage(page, wrapper);
        return result.getRecords().stream()
                .map(chatMessageConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatMessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessagePO::getSessionId, sessionId)
                .eq(ChatMessagePO::getIsDeleted, 0L);
        return chatMessageMapper.selectCount(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        ChatMessagePO po = chatMessageMapper.selectById(id);
        if (po != null) {
            po.setIsDeleted(System.currentTimeMillis());
            chatMessageMapper.updateById(po);
        }
    }
}
