package org.joker.comfypilot.resource.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * 文件记录DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileRecordDTO extends BaseDTO {

    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private String storageType;
    private String businessType;
    private Long businessId;
    private Long userId;
    private String accessUrl;
    private Boolean isPublic;
    private LocalDateTime expireTime;
}
