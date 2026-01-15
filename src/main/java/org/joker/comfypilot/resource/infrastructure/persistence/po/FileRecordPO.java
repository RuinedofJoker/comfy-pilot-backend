package org.joker.comfypilot.resource.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * 文件记录持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_record")
public class FileRecordPO extends BasePO {

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
