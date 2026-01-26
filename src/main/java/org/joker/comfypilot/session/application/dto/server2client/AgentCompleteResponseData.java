package org.joker.comfypilot.session.application.dto.server2client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.session.application.dto.ServerToClientMessage;

/**
 * Agent完成响应数据
 * 服务端 -> 客户端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent完成响应数据")
public class AgentCompleteResponseData implements ServerToClientMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 最大token数
     */
    @Schema(description = "最大token数", example = "200000")
    private Integer maxTokens;

    /**
     * 最大消息数
     */
    @Schema(description = "最大消息数", example = "500")
    private Integer maxMessages;

    /**
     * 输入token数
     */
    @Schema(description = "输入token数", example = "10000")
    private Integer inputTokens;

    /**
     * 输出token数
     */
    @Schema(description = "输出token数", example = "100")
    private Integer outputTokens;

    /**
     * 总token数
     */
    @Schema(description = "总token数", example = "10100")
    private Integer totalTokens;

    /**
     * 累计消息数
     */
    @Schema(description = "累计消息数", example = "30")
    private Integer messageCount;

}
