package org.joker.comfypilot.session.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.util.Map;

/**
 * 消息 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息信息")
public class ChatMessageDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "会话编码")
    private String sessionCode;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "消息角色")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息内容数据")
    private String contentData;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;
}
