package org.joker.comfypilot.session.infrastructure.persistence.converter;

import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatMessagePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 消息 PO 转换器
 */
@Mapper(componentModel = "spring")
public interface ChatMessageConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "messageRoleStringToEnum")
    @Mapping(target = "status", source = "status", qualifiedByName = "messageStatusStringToEnum")
    ChatMessage toDomain(ChatMessagePO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "messageRoleEnumToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "messageStatusEnumToString")
    ChatMessagePO toPO(ChatMessage domain);

    /**
     * 字符串转枚举
     */
    @Named("messageRoleStringToEnum")
    default MessageRole messageRoleStringToEnum(String value) {
        return value != null ? MessageRole.valueOf(value) : null;
    }

    /**
     * 枚举转字符串
     */
    @Named("messageRoleEnumToString")
    default String messageRoleEnumToString(MessageRole role) {
        return role != null ? role.name() : null;
    }

    /**
     * 字符串转枚举
     */
    @Named("messageStatusStringToEnum")
    default MessageStatus messageStatusStringToEnum(String value) {
        return value != null ? MessageStatus.valueOf(value) : null;
    }

    /**
     * 枚举转字符串
     */
    @Named("messageStatusEnumToString")
    default String messageStatusEnumToString(MessageStatus status) {
        return status != null ? status.name() : null;
    }
}
