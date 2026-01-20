package org.joker.comfypilot.session.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.infrastructure.persistence.po.ChatSessionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * 会话 PO 转换器
 */
@Mapper(componentModel = "spring")
public interface ChatSessionConverter {

    /**
     * PO转领域实体
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToEnum")
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "stringToMap")
    ChatSession toDomain(ChatSessionPO po);

    /**
     * 领域实体转PO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    @Mapping(target = "agentConfig", source = "agentConfig", qualifiedByName = "mapToString")
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

    @Named("stringToMap")
    default Map<String, Object> stringToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to deserialize advancedFeatures", e);
        }
    }

    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
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
