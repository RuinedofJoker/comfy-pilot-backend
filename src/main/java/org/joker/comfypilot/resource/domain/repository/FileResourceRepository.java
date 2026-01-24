package org.joker.comfypilot.resource.domain.repository;

import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.enums.FileSourceType;

import java.util.List;
import java.util.Optional;

/**
 * 文件资源仓储接口
 */
public interface FileResourceRepository {

    /**
     * 根据ID查询文件资源
     */
    Optional<FileResource> findById(Long id);

    /**
     * 根据存储文件来源和文件名查询
     */
    Optional<FileResource> findBySourceAndStoredName(String storedName, FileSourceType sourceType);

    /**
     * 查询用户上传的文件列表
     */
    List<FileResource> findByUploadUserId(Long userId);

    /**
     * 根据业务信息查询
     */
    List<FileResource> findByBusinessInfo(String businessType, Long businessId);

    /**
     * 保存文件资源
     */
    FileResource save(FileResource fileResource);

    /**
     * 删除文件资源
     */
    void deleteById(Long id);
}
