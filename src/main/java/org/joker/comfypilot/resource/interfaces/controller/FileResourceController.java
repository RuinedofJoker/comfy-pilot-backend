package org.joker.comfypilot.resource.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.resource.application.converter.FileResourceDTOConverter;
import org.joker.comfypilot.resource.application.service.FileDownloadService;
import org.joker.comfypilot.resource.application.service.FileManagementService;
import org.joker.comfypilot.resource.application.service.FileUploadService;
import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.interfaces.dto.FileResourceDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件资源控制器
 */
@Tag(name = "文件资源", description = "文件上传、下载、管理相关接口")
@RestController
@RequestMapping("/api/v1/files")
public class FileResourceController {

    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private FileDownloadService fileDownloadService;
    @Autowired
    private FileManagementService fileManagementService;
    @Autowired
    private FileResourceDTOConverter dtoConverter;

    /**
     * 上传单个文件
     */
    @Operation(summary = "上传单个文件", description = "上传单个文件到服务器，支持关联业务类型和业务ID")
    @PostMapping("/upload")
    public Result<FileResourceDTO> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) Long businessId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        FileResource fileResource = fileUploadService.uploadFile(file, userId, businessType, businessId);
        return Result.success(dtoConverter.toDTO(fileResource));
    }

    /**
     * 批量上传文件
     */
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到服务器")
    @PostMapping("/upload/batch")
    public Result<List<FileResourceDTO>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true) @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "业务类型") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) Long businessId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        List<FileResource> fileResources = fileUploadService.uploadFiles(files, userId, businessType, businessId);
        List<FileResourceDTO> dtos = fileResources.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 下载文件
     */
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "文件ID", required = true) @PathVariable Long fileId) {
        FileResource fileResource = fileDownloadService.downloadFile(fileId);
        InputStream inputStream = fileDownloadService.getFileInputStream(fileResource);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileResource.getFileType()))
                .contentLength(fileResource.getFileSize())
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "根据文件ID删除文件（逻辑删除）")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable Long fileId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        fileManagementService.deleteFile(fileId, userId);
        return Result.success();
    }

    /**
     * 查询用户文件列表
     */
    @Operation(summary = "查询用户文件列表", description = "获取当前用户上传的所有文件列表")
    @GetMapping("/user")
    public Result<List<FileResourceDTO>> listUserFiles(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        List<FileResource> fileResources = fileManagementService.listUserFiles(userId);
        List<FileResourceDTO> dtos = fileResources.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 查询业务关联文件
     */
    @Operation(summary = "查询业务关联文件", description = "根据业务类型和业务ID查询关联的文件列表")
    @GetMapping("/business")
    public Result<List<FileResourceDTO>> listBusinessFiles(
            @Parameter(description = "业务类型", required = true) @RequestParam("businessType") String businessType,
            @Parameter(description = "业务ID", required = true) @RequestParam("businessId") Long businessId) {

        List<FileResource> fileResources = fileManagementService.listBusinessFiles(businessType, businessId);
        List<FileResourceDTO> dtos = fileResources.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }
}
