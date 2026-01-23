package org.joker.comfypilot.session.application.dto.server2client;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Agent完成响应数据")
public class AgentCompleteResponseData implements ServerToClientMessage {

    private static final long serialVersionUID = 1L;

}
