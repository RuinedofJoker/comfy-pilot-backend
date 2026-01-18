package org.joker.comfypilot.cfsvr.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * ComfyUI工作流执行响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @JsonProperty("prompt_id")
    private String promptId;

    /**
     * 任务编号
     */
    private Integer number;

    /**
     * 节点错误信息
     */
    @JsonProperty("node_errors")
    private Map<String, Object> nodeErrors;
}
