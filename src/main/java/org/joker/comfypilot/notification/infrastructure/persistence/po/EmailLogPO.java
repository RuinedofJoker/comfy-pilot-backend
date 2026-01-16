package org.joker.comfypilot.notification.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * 邮件日志持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("email_log")
public class EmailLogPO extends BasePO {

    private static final long serialVersionUID = 1L;

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
    private String sendStatus;

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
}
