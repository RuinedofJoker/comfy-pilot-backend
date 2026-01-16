package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建工作流请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建工作流请求")
public class CreateWorkflowRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工作流名称", example = "我的工作流", required = true)
    @NotBlank(message = "工作流名称不能为空")
    @Size(min = 2, max = 100, message = "工作流名称长度必须在2-100个字符之间")
    private String workflowName;

    @Schema(description = "工作流描述", example = "这是一个测试工作流")
    @Size(max = 500, message = "工作流描述长度不能超过500个字符")
    private String description;

    @Schema(description = "所属ComfyUI服务ID", example = "1234567890", required = true)
    @NotNull(message = "ComfyUI服务ID不能为空")
    private Long comfyuiServerId;

    @Schema(description = "所属ComfyUI服务唯一标识符", example = "my-comfyui-server", required = true)
    @NotBlank(message = "ComfyUI服务Key不能为空")
    private String comfyuiServerKey;
}
