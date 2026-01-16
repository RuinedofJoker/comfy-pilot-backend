package org.joker.comfypilot.resource.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.repository.FileResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileResourceRepository fileResourceRepository;

    @Value("${file.storage.root-path}")
    private String rootPath;

    @Value("${file.storage.max-file-size}")
    private Long maxFileSize;

    @Value("${file.storage.allowed-extensions}")
    private String allowedExtensions;

    /**
     * 上传单个文件
     */
    public FileResource uploadFile(MultipartFile file, Long userId, String businessType, Long businessId) {
        // 验证文件
        validateFile(file);

        // 生成存储文件名
        String storedName = generateStoredName(file.getOriginalFilename());

        // 获取文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());

        // 创建存储目录
        Path storagePath = Paths.get(rootPath);
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            log.error("创建存储目录失败", e);
            throw new BusinessException("创建存储目录失败");
        }

        // 保存文件到磁盘
        Path filePath = storagePath.resolve(storedName);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("保存文件失败", e);
            throw new BusinessException("保存文件失败");
        }

        // 创建文件资源实体
        FileResource fileResource = FileResource.builder()
                .fileName(file.getOriginalFilename())
                .storedName(storedName)
                .filePath(rootPath)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .fileExtension(extension)
                .uploadUserId(userId)
                .businessType(businessType)
                .businessId(businessId)
                .downloadCount(0)
                .createTime(LocalDateTime.now())
                .createBy(userId)
                .updateTime(LocalDateTime.now())
                .updateBy(userId)
                .build();

        // 保存到数据库
        return fileResourceRepository.save(fileResource);
    }

    /**
     * 批量上传文件
     */
    public List<FileResource> uploadFiles(List<MultipartFile> files, Long userId, String businessType, Long businessId) {
        List<FileResource> result = new ArrayList<>();
        for (MultipartFile file : files) {
            FileResource fileResource = uploadFile(file, userId, businessType, businessId);
            result.add(fileResource);
        }
        return result;
    }

    /**
     * 生成唯一存储文件名
     */
    public String generateStoredName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 验证文件（类型、大小）
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制：" + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 验证文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!extension.isEmpty()) {
            List<String> allowedList = Arrays.asList(allowedExtensions.split(","));
            if (!allowedList.contains(extension)) {
                throw new BusinessException("不支持的文件类型：" + extension);
            }
        }
    }
}
