package org.joker.comfypilot.notification.domain.repository;

import org.joker.comfypilot.notification.domain.entity.EmailLog;

import java.util.List;
import java.util.Optional;

/**
 * 邮件日志仓储接口
 */
public interface EmailLogRepository {

    /**
     * 根据ID查询邮件日志
     */
    Optional<EmailLog> findById(Long id);

    /**
     * 查询收件人的邮件日志
     */
    List<EmailLog> findByRecipient(String recipient);

    /**
     * 根据业务信息查询
     */
    List<EmailLog> findByBusinessInfo(String businessType, String businessId);

    /**
     * 保存邮件日志
     */
    EmailLog save(EmailLog emailLog);
}
