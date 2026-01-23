package org.joker.comfypilot.session.application.dto;

/**
 * 客户端到服务端的消息标记接口
 * 所有从客户端发送到服务端的消息数据类型都应实现此接口
 *
 * <p>位于 {@code org.joker.comfypilot.session.application.dto.client2server} 包下的类应实现此接口
 */
public interface ClientToServerMessage extends WebSocketMessageData {
}
