package org.joker.comfypilot.session.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 会话 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话信息")
public class ChatSessionDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话编码")
    private String sessionCode;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "Agent ID")
    private Long agentId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "会话状态（ACTIVE, CLOSED）")
    private String status;
}
