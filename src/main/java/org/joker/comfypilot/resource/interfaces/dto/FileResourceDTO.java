package org.joker.comfypilot.resource.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 文件资源DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件资源信息")
public class FileResourceDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "存储文件名")
    private String storedName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件MIME类型")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务关联ID")
    private Long businessId;

    @Schema(description = "下载次数")
    private Integer downloadCount;
}
