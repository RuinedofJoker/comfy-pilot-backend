package org.joker.comfypilot.notification.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.notification.domain.entity.EmailLog;
import org.joker.comfypilot.notification.domain.repository.EmailLogRepository;
import org.joker.comfypilot.notification.infrastructure.persistence.converter.EmailLogConverter;
import org.joker.comfypilot.notification.infrastructure.persistence.mapper.EmailLogMapper;
import org.joker.comfypilot.notification.infrastructure.persistence.po.EmailLogPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 邮件日志仓储实现
 */
@Repository
public class EmailLogRepositoryImpl implements EmailLogRepository {

    @Autowired
    private EmailLogMapper emailLogMapper;
    @Autowired
    private EmailLogConverter emailLogConverter;

    @Override
    public Optional<EmailLog> findById(Long id) {
        EmailLogPO po = emailLogMapper.selectById(id);
        return Optional.ofNullable(emailLogConverter.toDomain(po));
    }

    @Override
    public List<EmailLog> findByRecipient(String recipient) {
        LambdaQueryWrapper<EmailLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailLogPO::getRecipient, recipient);
        List<EmailLogPO> pos = emailLogMapper.selectList(wrapper);
        return pos.stream()
                .map(emailLogConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailLog> findByBusinessInfo(String businessType, String businessId) {
        LambdaQueryWrapper<EmailLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailLogPO::getBusinessType, businessType)
                .eq(EmailLogPO::getBusinessId, businessId);
        List<EmailLogPO> pos = emailLogMapper.selectList(wrapper);
        return pos.stream()
                .map(emailLogConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public EmailLog save(EmailLog emailLog) {
        EmailLogPO po = emailLogConverter.toPO(emailLog);
        if (po.getId() == null) {
            emailLogMapper.insert(po);
        } else {
            emailLogMapper.updateById(po);
        }
        return emailLogConverter.toDomain(po);
    }
}
