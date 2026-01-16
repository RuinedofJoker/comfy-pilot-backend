package org.joker.comfypilot.notification.infrastructure.persistence.converter;

import org.joker.comfypilot.notification.domain.entity.EmailLog;
import org.joker.comfypilot.notification.domain.enums.EmailSendStatus;
import org.joker.comfypilot.notification.infrastructure.persistence.po.EmailLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 邮件日志转换器
 */
@Mapper(componentModel = "spring")
public interface EmailLogConverter {

    /**
     * PO转领域实体
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    @Mapping(target = "sendStatus", source = "sendStatus", qualifiedByName = "stringToEnum")
    EmailLog toDomain(EmailLogPO po);

    /**
     * 领域实体转PO
     *
     * @param domain 领域实体
     * @return 持久化对象
     */
    @Mapping(target = "sendStatus", source = "sendStatus", qualifiedByName = "enumToString")
    EmailLogPO toPO(EmailLog domain);

    /**
     * String转EmailSendStatus枚举
     */
    @Named("stringToEnum")
    default EmailSendStatus stringToEnum(String value) {
        return value != null ? EmailSendStatus.valueOf(value) : null;
    }

    /**
     * EmailSendStatus枚举转String
     */
    @Named("enumToString")
    default String enumToString(EmailSendStatus status) {
        return status != null ? status.name() : null;
    }
}
