package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.UserMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.content.ChatContent;

import java.util.List;

/**
 * 用户消息
 * 支持多模态内容（文本、图片、音频、视频、PDF）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("user")
public class ChatUserMessage implements PersistableChatMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 消息内容列表（支持多模态）
     */
    private List<ChatContent> contents;

    /**
     * 转换为LangChain4j的UserMessage
     */
    public UserMessage toUserMessage() {
        return UserMessage.from(contents.stream().map(ChatContent::toContent).toList());
    }

    /**
     * 从LangChain4j的UserMessage创建ChatUserMessage
     */
    public static ChatUserMessage from(UserMessage userMessage) {
        List<ChatContent> chatContents = userMessage.contents().stream()
                .map(ChatContent::from)
                .toList();
        return new ChatUserMessage(chatContents);
    }

}
