package org.joker.comfypilot.notification.domain.enums;

/**
 * 邮件发送状态枚举
 */
public enum EmailSendStatus {

    /**
     * 待发送
     */
    PENDING("待发送"),

    /**
     * 发送成功
     */
    SUCCESS("发送成功"),

    /**
     * 发送失败
     */
    FAILED("发送失败");

    private final String description;

    EmailSendStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
