package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.SystemMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统消息
 * 用于设置AI的角色、行为规则和上下文信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("system")
public class ChatSystemMessage implements PersistableChatMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 系统提示词内容
     */
    private String content;

    /**
     * 转换为LangChain4j的SystemMessage
     */
    public SystemMessage toSystemMessage() {
        return SystemMessage.from(content);
    }

    /**
     * 从LangChain4j的SystemMessage创建ChatSystemMessage
     */
    public static ChatSystemMessage from(SystemMessage systemMessage) {
        return new ChatSystemMessage(systemMessage.text());
    }

}
