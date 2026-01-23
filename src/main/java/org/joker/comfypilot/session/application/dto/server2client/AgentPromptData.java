package org.joker.comfypilot.session.application.dto.server2client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.session.application.dto.ServerToClientMessage;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;

/**
 * Agent提示消息数据
 * 服务端 -> 客户端
 *
 * <p>用于统一处理Agent执行过程中的各种状态提示消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent提示消息数据")
public class AgentPromptData implements ServerToClientMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 提示类型
     */
    @Schema(description = "提示类型", example = "THINKING")
    private AgentPromptType promptType;

    /**
     * 提示内容（可选，如果为空则使用默认提示）
     */
    @Schema(description = "提示内容", example = "正在分析代码结构...")
    private String message;
}
