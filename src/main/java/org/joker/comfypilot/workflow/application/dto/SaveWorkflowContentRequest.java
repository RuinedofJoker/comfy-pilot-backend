package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 保存工作流内容请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "保存工作流内容请求")
public class SaveWorkflowContentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工作流内容（JSON格式）", example = "{\"nodes\": [], \"links\": []}", required = true)
    @NotBlank(message = "工作流内容不能为空")
    private String content;
}
