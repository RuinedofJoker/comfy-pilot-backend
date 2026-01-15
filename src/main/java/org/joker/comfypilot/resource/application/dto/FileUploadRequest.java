package org.joker.comfypilot.resource.application.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求
 */
@Data
public class FileUploadRequest {

    private MultipartFile file;
    private String businessType;
    private Long businessId;
    private Boolean isPublic;
}
