package org.joker.comfypilot.session.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空的WebSocket消息数据
 * 用于不需要携带额外数据的消息类型
 *
 * <p>适用的消息类型：
 * <ul>
 *   <li>INTERRUPT - 中断执行</li>
 *   <li>PING/PONG - 心跳消息</li>
 *   <li>AGENT_THINKING - Agent思考中</li>
 *   <li>SUMMERY/SUMMERY_COMPLETE - 摘要相关</li>
 *   <li>AGENT_STREAM - Agent流式输出</li>
 *   <li>EXECUTION_INTERRUPTED - 执行中断完成</li>
 *   <li>ERROR - 错误消息</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class VoidWebSocketMessageData implements WebSocketMessageData {

    private static final long serialVersionUID = 1L;
}
