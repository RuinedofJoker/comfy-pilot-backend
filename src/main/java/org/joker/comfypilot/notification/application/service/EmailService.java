package org.joker.comfypilot.notification.application.service;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.notification.domain.entity.EmailLog;
import org.joker.comfypilot.notification.domain.enums.EmailSendStatus;
import org.joker.comfypilot.notification.domain.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Autowired
    private EmailLogRepository emailLogRepository;

    /**
     * 发送普通邮件
     */
    public void sendEmail(String recipient, String subject, String content) {
        sendEmail(recipient, subject, content, false, null, null);
    }

    /**
     * 发送HTML邮件
     */
    public void sendHtmlEmail(String recipient, String subject, String htmlContent) {
        sendEmail(recipient, subject, htmlContent, true, null, null);
    }

    /**
     * 异步发送邮件
     */
    @Async
    public void sendEmailAsync(String recipient, String subject, String content) {
        sendEmail(recipient, subject, content, false, null, null);
    }

    /**
     * 发送密码重置邮件
     */
    public void sendPasswordResetEmail(String recipient, String resetToken) {
        String subject = "密码重置";
        String content = buildPasswordResetEmailContent(resetToken);
        sendEmail(recipient, subject, content, true, "PASSWORD_RESET", resetToken);
    }

    /**
     * 发送邮件核心方法
     */
    private void sendEmail(String recipient, String subject, String content,
                          boolean isHtml, String businessType, String businessId) {
        // 创建邮件日志
        EmailLog emailLog = EmailLog.builder()
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .sendStatus(EmailSendStatus.PENDING)
                .retryCount(0)
                .businessType(businessType)
                .businessId(businessId)
                .createTime(LocalDateTime.now())
                .createBy(0L)
                .updateTime(LocalDateTime.now())
                .updateBy(0L)
                .build();

        emailLog = emailLogRepository.save(emailLog);

        try {
            // 创建邮件消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            // 发送邮件
            mailSender.send(message);

            // 标记为发送成功
            emailLog.markAsSent();
            emailLogRepository.save(emailLog);

            log.info("邮件发送成功: recipient={}, subject={}", recipient, subject);

        } catch (MessagingException e) {
            log.error("邮件发送失败: recipient={}, subject={}", recipient, subject, e);

            // 标记为发送失败
            emailLog.markAsFailed(e.getMessage());
            emailLog.incrementRetryCount();
            emailLogRepository.save(emailLog);

            throw new BusinessException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildPasswordResetEmailContent(String resetToken) {
        return "<html>" +
                "<body>" +
                "<h2>密码重置</h2>" +
                "<p>您好，</p>" +
                "<p>您请求重置密码。请使用以下验证码完成密码重置：</p>" +
                "<h3 style='color: #007bff;'>" + resetToken + "</h3>" +
                "<p>该验证码将在15分钟后失效。</p>" +
                "<p>如果这不是您的操作，请忽略此邮件。</p>" +
                "<br>" +
                "<p>ComfyUI Pilot 团队</p>" +
                "</body>" +
                "</html>";
    }
}
