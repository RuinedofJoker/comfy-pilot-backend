package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建工作流版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建工作流版本请求")
public class CreateVersionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "版本内容（JSON格式）", example = "{\"nodes\": [], \"links\": []}", required = true)
    @NotBlank(message = "版本内容不能为空")
    private String content;

    @Schema(description = "变更摘要", example = "添加了图像处理节点")
    @Size(max = 500, message = "变更摘要长度不能超过500个字符")
    private String changeSummary;

    @Schema(description = "关联的会话ID", example = "5555555555")
    private Long sessionId;
}
