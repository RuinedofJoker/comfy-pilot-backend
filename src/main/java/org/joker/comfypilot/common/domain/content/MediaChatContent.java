package org.joker.comfypilot.common.domain.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 多媒体内容抽象基类
 * 处理Base64和URL两种数据格式的共同逻辑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class MediaChatContent implements ChatContent, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否使用Base64编码
     */
    private Boolean isUseBase64;

    /**
     * 资源URL（当isUseBase64=false时使用）
     */
    private String url;

    /**
     * Base64编码数据（当isUseBase64=true时使用）
     */
    private String base64Data;

    /**
     * MIME类型（如image/png, audio/mp3）
     */
    private String mimeType;

}
