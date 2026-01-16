package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新API密钥请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新API密钥请求")
public class UpdateApiKeyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "密钥名称")
    @Size(max = 100, message = "密钥名称长度不能超过100")
    private String keyName;
}
