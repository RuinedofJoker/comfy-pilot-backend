package org.joker.comfypilot.common.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.exception.BusinessException;

import java.net.MalformedURLException;

/**
 * 音频内容
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("audio")
public class AudioChatContent extends MediaChatContent {

    private static final long serialVersionUID = 1L;

    public AudioChatContent(Boolean isUseBase64, String url, String base64Data, String mimeType) {
        super(isUseBase64, url, base64Data, mimeType);
    }

    @Override
    public Content toContent() {
        if (Boolean.TRUE.equals(getIsUseBase64())) {
            return new AudioContent(getBase64Data(), getMimeType());
        } else {
            return new AudioContent(getUrl());
        }
    }

    public static AudioChatContent from(AudioContent audioContent) {
        AudioChatContent content = new AudioChatContent();
        if (StringUtils.isNotBlank(audioContent.audio().base64Data())) {
            content.setIsUseBase64(true);
            content.setBase64Data(audioContent.audio().base64Data());
            content.setMimeType(audioContent.audio().mimeType());
        } else {
            content.setIsUseBase64(false);
            try {
                content.setUrl(audioContent.audio().url().toURL().toString());
            } catch (MalformedURLException e) {
                log.error("Malformed URL", e);
                throw new BusinessException("根据URI对象获取url出错");
            }
        }
        return content;
    }

}
