package org.joker.comfypilot.common.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.exception.BusinessException;

import java.net.MalformedURLException;

/**
 * 图片内容
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("image")
public class ImageChatContent extends MediaChatContent {

    private static final long serialVersionUID = 1L;

    public ImageChatContent(Boolean isUseBase64, String url, String base64Data, String mimeType) {
        super(isUseBase64, url, base64Data, mimeType);
    }

    @Override
    public Content toContent() {
        if (Boolean.TRUE.equals(getIsUseBase64())) {
            return new ImageContent(getBase64Data(), getMimeType());
        } else {
            return new ImageContent(getUrl());
        }
    }

    public static ImageChatContent from(ImageContent imageContent) {
        ImageChatContent content = new ImageChatContent();
        if (StringUtils.isNotBlank(imageContent.image().base64Data())) {
            content.setIsUseBase64(true);
            content.setBase64Data(imageContent.image().base64Data());
            content.setMimeType(imageContent.image().mimeType());
        } else {
            content.setIsUseBase64(false);
            try {
                content.setUrl(imageContent.image().url().toURL().toString());
            } catch (MalformedURLException e) {
                log.error("Malformed URL", e);
                throw new BusinessException("根据URI对象获取url出错");
            }
        }
        return content;
    }

}
