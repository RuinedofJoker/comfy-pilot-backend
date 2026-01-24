package org.joker.comfypilot.session.application.dto.server2client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.session.application.dto.ServerToClientMessage;

/**
 * Agent工具调用请求数据
 * 服务端 -> 客户端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent工具调用请求数据")
public class AgentToolCallRequestData implements ServerToClientMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 工具调用id
     */
    @Schema(description = "工具调用id", example = "xxx")
    private String toolCallId;

    /**
     * 工具名称
     */
    @Schema(description = "工具名称", example = "readFile")
    private String toolName;

    /**
     * 工具参数（JSON字符串）
     */
    @Schema(description = "工具参数（JSON字符串）", example = "{\"path\": \"/path/to/file\"}")
    private String toolArgs;

    /**
     * 是否是客户端工具
     */
    @Schema(description = "是否是客户端工具，如果不是只需要返回是否允许执行就行了", example = "true")
    private Boolean isClientTool;
}
