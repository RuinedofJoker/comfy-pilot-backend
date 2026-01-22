package org.joker.comfypilot.common.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("text")
public class TextChatContent implements ChatContent {

    private static final long serialVersionUID = 1L;

    private String text;

    @Override
    public Content toContent() {
        return TextContent.from(text);
    }

    public static TextChatContent from(TextContent textContent) {
        return new TextChatContent(textContent.text());
    }

}
