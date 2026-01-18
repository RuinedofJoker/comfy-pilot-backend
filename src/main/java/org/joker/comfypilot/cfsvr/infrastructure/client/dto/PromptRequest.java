package org.joker.comfypilot.cfsvr.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * ComfyUI工作流执行请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流定义（节点图）
     */
    private Map<String, Object> prompt;

    /**
     * 客户端ID（用于WebSocket订阅）
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 额外数据
     */
    @JsonProperty("extra_data")
    private ExtraData extraData;

    /**
     * 额外数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraData implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * ComfyUI API密钥（用于调用付费节点）
         */
        @JsonProperty("api_key")
        private String apiKey;
    }
}
