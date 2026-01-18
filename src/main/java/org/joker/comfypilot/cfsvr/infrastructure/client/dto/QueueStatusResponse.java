package org.joker.comfypilot.cfsvr.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ComfyUI队列状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 正在运行的任务列表
     */
    @JsonProperty("queue_running")
    private List<Object> queueRunning;

    /**
     * 待执行的任务列表
     */
    @JsonProperty("queue_pending")
    private List<Object> queuePending;
}
