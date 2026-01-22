package org.joker.comfypilot.common.domain.message;

import dev.langchain4j.data.message.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.domain.content.ChatContent;
import org.joker.comfypilot.common.exception.BusinessException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class ChatUserMessage implements ChatMessage {

    private List<ChatContent> contents;

    public UserMessage toUserMessage() {
        return UserMessage.from(contents.stream().map(ChatContent::toContent).toList());
    }

    public static ChatUserMessage fromUserMessage(UserMessage userMessage) {
        List<ChatContent> chatContents = new ArrayList<>(userMessage.contents().size());
        for (Content content : userMessage.contents()) {
            ChatContent chatContent = new ChatContent();
            switch (content) {
                case ImageContent imageContent -> {
                    if (StringUtils.isNotBlank(imageContent.image().base64Data())) {
                        chatContent.setIsUseBase64(true);
                        chatContent.setBase64Data(imageContent.image().base64Data());
                    } else {
                        chatContent.setIsUseBase64(false);
                        try {
                            chatContent.setUrl(imageContent.image().url().toURL().toString());
                        } catch (MalformedURLException e) {
                            log.error("Malformed URL", e);
                            throw new BusinessException("根据URId对象获取url出错");
                        }
                    }
                    chatContent.setType("image");
                }
                case AudioContent audioContent -> {
                    if (StringUtils.isNotBlank(audioContent.audio().base64Data())) {
                        chatContent.setIsUseBase64(true);
                        chatContent.setBase64Data(audioContent.audio().base64Data());
                    } else {
                        chatContent.setIsUseBase64(false);
                        try {
                            chatContent.setUrl(audioContent.audio().url().toURL().toString());
                        } catch (MalformedURLException e) {
                            log.error("Malformed URL", e);
                            throw new BusinessException("根据URId对象获取url出错");
                        }
                    }
                    chatContent.setType("audio");
                }
                case VideoContent videoContent -> {
                    if (StringUtils.isNotBlank(videoContent.video().base64Data())) {
                        chatContent.setIsUseBase64(true);
                        chatContent.setBase64Data(videoContent.video().base64Data());
                    } else {
                        chatContent.setIsUseBase64(false);
                        try {
                            chatContent.setUrl(videoContent.video().url().toURL().toString());
                        } catch (MalformedURLException e) {
                            log.error("Malformed URL", e);
                            throw new BusinessException("根据URId对象获取url出错");
                        }
                    }
                    chatContent.setType("video");
                }
                case PdfFileContent pdfFileContent -> {
                    if (StringUtils.isNotBlank(pdfFileContent.pdfFile().base64Data())) {
                        chatContent.setIsUseBase64(true);
                        chatContent.setBase64Data(pdfFileContent.pdfFile().base64Data());
                    } else {
                        chatContent.setIsUseBase64(false);
                        try {
                            chatContent.setUrl(pdfFileContent.pdfFile().url().toURL().toString());
                        } catch (MalformedURLException e) {
                            log.error("Malformed URL", e);
                            throw new BusinessException("根据URId对象获取url出错");
                        }
                    }
                    chatContent.setType("pdfFile");
                }
                default -> {
                    chatContent.setText(((TextContent) content).text());
                    chatContent.setType("text");
                }
            }
            chatContents.add(chatContent);
        }
        ChatUserMessage chatUserMessage = new ChatUserMessage();
        chatUserMessage.contents = chatContents;
        return chatUserMessage;
    }

}
