package org.joker.comfypilot.resource.application.service;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.enums.FileSourceType;
import org.joker.comfypilot.resource.domain.repository.FileResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 文件管理服务
 */
@Slf4j
@Service
public class FileManagementService {

    @Autowired
    private FileResourceRepository fileResourceRepository;

    /**
     * 删除文件（物理删除+逻辑删除）
     */
    public void deleteFile(String storedName, Long userId) {
        FileResource fileResource = fileResourceRepository.findBySourceAndStoredName(storedName, FileSourceType.SERVER_LOCAL)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        // 验证权限：只有上传者可以删除
        if (!fileResource.getUploadUserId().equals(userId)) {
            throw new BusinessException("无权删除该文件");
        }

        // 物理删除文件
        Path filePath = Paths.get(fileResource.getFilePath(), fileResource.getStoredName());
        try {
            Files.deleteIfExists(filePath);
            log.info("物理删除文件成功: {}", filePath);
        } catch (IOException e) {
            log.error("物理删除文件失败: {}", filePath, e);
            // 物理删除失败不影响逻辑删除
        }

        // 逻辑删除
        fileResourceRepository.deleteById(fileResource.getId());
    }

    /**
     * 查询用户文件列表
     */
    public List<FileResource> listUserFiles(Long userId) {
        return fileResourceRepository.findByUploadUserId(userId);
    }

    /**
     * 查询业务关联文件
     */
    public List<FileResource> listBusinessFiles(String businessType, Long businessId) {
        return fileResourceRepository.findByBusinessInfo(businessType, businessId);
    }
}
