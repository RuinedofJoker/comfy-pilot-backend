package org.joker.comfypilot.session.application.converter;

import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 会话 DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface ChatSessionDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    ChatSessionDTO toDTO(ChatSession entity);

    /**
     * 消息Entity转DTO
     */
    @Mapping(target = "role", source = "role", qualifiedByName = "roleEnumToString")
    ChatMessageDTO toMessageDTO(ChatMessage entity);

    /**
     * 枚举转字符串
     */
    @Named("enumToString")
    default String enumToString(Enum<?> status) {
        return status != null ? status.name() : null;
    }

    /**
     * 角色枚举转字符串
     */
    @Named("roleEnumToString")
    default String roleEnumToString(Enum<?> role) {
        return role != null ? role.name() : null;
    }
}
