package org.joker.comfypilot.resource.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 文件资源领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResource extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 文件资源ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 存储文件名（唯一）
     */
    private String storedName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务关联ID
     */
    private Long businessId;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        if (this.downloadCount == null) {
            this.downloadCount = 0;
        }
        this.downloadCount++;
    }

    /**
     * 更新业务关联信息
     */
    public void updateBusinessInfo(String businessType, Long businessId) {
        this.businessType = businessType;
        this.businessId = businessId;
    }

    /**
     * 获取完整文件路径
     */
    public String getFullPath() {
        return filePath + "/" + storedName;
    }
}
