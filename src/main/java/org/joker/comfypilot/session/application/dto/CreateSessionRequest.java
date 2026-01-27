package org.joker.comfypilot.session.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建会话请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建会话请求")
public class CreateSessionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ComfyUI服务ID")
    private Long comfyuiServerId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "会话规则")
    private String rules;

}
