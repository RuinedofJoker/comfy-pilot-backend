package org.joker.comfypilot.common.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.PdfFileContent;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.exception.BusinessException;

import java.net.MalformedURLException;

/**
 * PDF文件内容
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("pdfFile")
public class PdfChatContent extends MediaChatContent {

    private static final long serialVersionUID = 1L;

    public PdfChatContent(Boolean isUseBase64, String url, String base64Data, String mimeType) {
        super(isUseBase64, url, base64Data, mimeType);
    }

    @Override
    public Content toContent() {
        if (Boolean.TRUE.equals(getIsUseBase64())) {
            return new PdfFileContent(getBase64Data(), getMimeType());
        } else {
            return new PdfFileContent(getUrl());
        }
    }

    public static PdfChatContent from(PdfFileContent pdfFileContent) {
        PdfChatContent content = new PdfChatContent();
        if (StringUtils.isNotBlank(pdfFileContent.pdfFile().base64Data())) {
            content.setIsUseBase64(true);
            content.setBase64Data(pdfFileContent.pdfFile().base64Data());
            content.setMimeType(pdfFileContent.pdfFile().mimeType());
        } else {
            content.setIsUseBase64(false);
            try {
                content.setUrl(pdfFileContent.pdfFile().url().toURL().toString());
            } catch (MalformedURLException e) {
                log.error("Malformed URL", e);
                throw new BusinessException("根据URI对象获取url出错");
            }
        }
        return content;
    }

}
