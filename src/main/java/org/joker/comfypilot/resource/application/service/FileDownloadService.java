package org.joker.comfypilot.resource.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.repository.FileResourceRepository;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileResourceRepository fileResourceRepository;

    /**
     * 根据ID下载文件
     */
    public FileResource downloadFile(Long fileId) {
        FileResource fileResource = fileResourceRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        // 增加下载次数
        fileResource.incrementDownloadCount();
        fileResourceRepository.save(fileResource);

        return fileResource;
    }

    /**
     * 根据存储名下载文件
     */
    public FileResource downloadFileByStoredName(String storedName) {
        FileResource fileResource = fileResourceRepository.findByStoredName(storedName)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        // 增加下载次数
        fileResource.incrementDownloadCount();
        fileResourceRepository.save(fileResource);

        return fileResource;
    }

    /**
     * 获取文件输入流
     */
    public InputStream getFileInputStream(FileResource fileResource) {
        Path filePath = Paths.get(fileResource.getFilePath(), fileResource.getStoredName());
        try {
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException e) {
            log.error("文件不存在: {}", filePath, e);
            throw new BusinessException("文件不存在");
        }
    }
}
