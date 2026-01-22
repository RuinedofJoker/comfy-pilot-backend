package org.joker.comfypilot.common.domain.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.langchain4j.data.message.*;

/**
 * 聊天内容接口
 * 用于在持久化格式和LangChain4j格式之间转换
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextChatContent.class, name = "text"),
        @JsonSubTypes.Type(value = ImageChatContent.class, name = "image"),
        @JsonSubTypes.Type(value = AudioChatContent.class, name = "audio"),
        @JsonSubTypes.Type(value = VideoChatContent.class, name = "video"),
        @JsonSubTypes.Type(value = PdfChatContent.class, name = "pdfFile")
})
public interface ChatContent {

    /**
     * 转换为LangChain4j的Content对象
     */
    Content toContent();

    /**
     * 从LangChain4j的Content对象创建ChatContent
     */
    static ChatContent from(Content content) {
        return switch (content) {
            case ImageContent imageContent -> ImageChatContent.from(imageContent);
            case AudioContent audioContent -> AudioChatContent.from(audioContent);
            case VideoContent videoContent -> VideoChatContent.from(videoContent);
            case PdfFileContent pdfFileContent -> PdfChatContent.from(pdfFileContent);
            case TextContent textContent -> TextChatContent.from(textContent);
            default -> throw new IllegalArgumentException("不支持的Content类型: " + content.getClass());
        };
    }

}
