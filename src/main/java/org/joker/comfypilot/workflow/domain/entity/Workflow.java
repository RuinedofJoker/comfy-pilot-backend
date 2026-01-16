package org.joker.comfypilot.workflow.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.common.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * 工作流领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 所属ComfyUI服务ID
     */
    private Long comfyuiServerId;

    /**
     * 所属ComfyUI服务唯一标识符
     */
    private String comfyuiServerKey;

    /**
     * 当前激活版本的内容（JSON格式）
     */
    private String activeContent;

    /**
     * 激活内容的SHA-256哈希值
     */
    private String activeContentHash;

    /**
     * 工作流缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 锁定人ID
     */
    private Long lockedBy;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedAt;

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

    // ==================== 业务方法 ====================

    /**
     * 保存工作流内容并计算哈希
     *
     * @param content 工作流内容（JSON格式）
     */
    public void saveContent(String content) {
        if (this.isLocked != null && this.isLocked) {
            throw new BusinessException("工作流已锁定，无法保存内容");
        }
        this.activeContent = content;
        this.activeContentHash = calculateContentHash(content);
    }

    /**
     * 获取当前激活内容
     *
     * @return 激活内容
     */
    public String getContent() {
        return this.activeContent;
    }

    /**
     * 计算内容的SHA-256哈希值
     *
     * @param content 内容
     * @return SHA-256哈希值（十六进制字符串）
     */
    public static String calculateContentHash(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(500, "计算内容哈希失败", e);
        }
    }

    /**
     * 锁定工作流
     *
     * @param userId 锁定人ID
     */
    public void lock(Long userId) {
        if (this.isLocked != null && this.isLocked) {
            throw new BusinessException("工作流已被锁定");
        }
        this.isLocked = true;
        this.lockedBy = userId;
        this.lockedAt = LocalDateTime.now();
    }

    /**
     * 解锁工作流
     */
    public void unlock() {
        this.isLocked = false;
        this.lockedBy = null;
        this.lockedAt = null;
    }

    /**
     * 判断是否被指定用户锁定
     *
     * @param userId 用户ID
     * @return 是否被指定用户锁定
     */
    public boolean isLockedBy(Long userId) {
        return this.isLocked != null && this.isLocked
            && this.lockedBy != null && this.lockedBy.equals(userId);
    }

    /**
     * 判断用户是否可以编辑
     *
     * @param userId 用户ID
     * @return 是否可以编辑
     */
    public boolean canEdit(Long userId) {
        // 未锁定或被当前用户锁定时可以编辑
        return (this.isLocked == null || !this.isLocked) || isLockedBy(userId);
    }
}
