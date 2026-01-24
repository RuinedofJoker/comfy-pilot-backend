package org.joker.comfypilot.session.application.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * 会话 DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface ChatSessionDTOConverter {

    /**
     * Entity转DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "mapToString")
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

    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to deserialize advancedFeatures", e);
        }
    }
}
