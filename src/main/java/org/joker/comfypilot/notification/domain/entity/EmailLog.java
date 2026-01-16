package org.joker.comfypilot.notification.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.notification.domain.enums.EmailSendStatus;

import java.time.LocalDateTime;

/**
 * 邮件日志领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 邮件日志ID
     */
    private Long id;

    /**
     * 收件人邮箱
     */
    private String recipient;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 发送状态
     */
    private EmailSendStatus sendStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 实际发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务关联ID
     */
    private String businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 标记为发送成功
     */
    public void markAsSent() {
        this.sendStatus = EmailSendStatus.SUCCESS;
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 标记为发送失败
     */
    public void markAsFailed(String errorMessage) {
        this.sendStatus = EmailSendStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        this.retryCount++;
    }

    /**
     * 判断是否可以重试（最多重试3次）
     */
    public boolean canRetry() {
        return this.retryCount != null && this.retryCount < 3;
    }
}
