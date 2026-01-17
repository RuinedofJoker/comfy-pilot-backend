package org.joker.comfypilot.session.infrastructure.persistence.converter;

import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatSessionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 会话 PO 转换器
 */
@Mapper(componentModel = "spring")
public interface ChatSessionConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToEnum")
    ChatSession toDomain(ChatSessionPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    ChatSessionPO toPO(ChatSession domain);

    /**
     * 字符串转枚举
     */
    @Named("stringToEnum")
    default SessionStatus stringToEnum(String value) {
        return value != null ? SessionStatus.valueOf(value) : null;
    }

    /**
     * 枚举转字符串
     */
    @Named("enumToString")
    default String enumToString(SessionStatus status) {
        return status != null ? status.name() : null;
    }
}
