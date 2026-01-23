package org.joker.comfypilot.session.application.dto;

/**
 * 服务端到客户端的消息标记接口
 * 所有从服务端发送到客户端的消息数据类型都应实现此接口
 *
 * <p>位于 {@code org.joker.comfypilot.session.application.dto.server2client} 包下的类应实现此接口
 */
public interface ServerToClientMessage extends WebSocketMessageData {
}
