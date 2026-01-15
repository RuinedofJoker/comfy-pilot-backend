package org.joker.comfypilot.resource.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.resource.domain.enums.BusinessType;
import org.joker.comfypilot.resource.domain.enums.FileType;
import org.joker.comfypilot.resource.domain.enums.StorageType;

import java.time.LocalDateTime;

/**
 * 文件记录领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileRecord extends BaseEntity<Long> {

    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private FileType fileType;
    private String mimeType;
    private StorageType storageType;
    private BusinessType businessType;
    private Long businessId;
    private Long userId;
    private String accessUrl;
    private Boolean isPublic;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置为公开
     */
    public void makePublic() {
        // TODO: 实现公开逻辑
    }

    /**
     * 设置为私有
     */
    public void makePrivate() {
        // TODO: 实现私有逻辑
    }

    /**
     * 设置过期时间
     */
    public void setExpiration(LocalDateTime expireTime) {
        // TODO: 实现过期时间设置逻辑
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        // TODO: 实现过期判断逻辑
        return false;
    }

    /**
     * 判断是否为临时文件
     */
    public boolean isTemporary() {
        return BusinessType.TEMP_FILE.equals(this.businessType);
    }

    /**
     * 生成访问URL
     */
    public String generateAccessUrl() {
        // TODO: 实现URL生成逻辑
        return null;
    }
}
