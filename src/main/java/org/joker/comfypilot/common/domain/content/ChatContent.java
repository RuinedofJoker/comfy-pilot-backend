package org.joker.comfypilot.common.domain.content;

import dev.langchain4j.data.message.*;
import lombok.Data;

@Data
public class ChatContent {

    private String type;

    private String text;

    private Boolean isUseBase64;

    private String url;

    private String base64Data;

    private String mimeType;

    public Content toContent() {
        switch (type) {
            case "image" -> {
                if (isUseBase64) {
                    return new ImageContent(base64Data, mimeType);
                } else {
                    return new ImageContent(url);
                }
            }
            case "audio" -> {
                if (isUseBase64) {
                    return new AudioContent(base64Data, mimeType);
                } else {
                    return new AudioContent(url);
                }
            }
            case "video" -> {
                if (isUseBase64) {
                    return new VideoContent(base64Data, mimeType);
                } else {
                    return new VideoContent(url);
                }
            }
            case "pdfFile" -> {
                if (isUseBase64) {
                    return new PdfFileContent(base64Data, mimeType);
                } else {
                    return new PdfFileContent(url);
                }
            }
            default -> {
                return TextContent.from(text);
            }
        }
    }

}
