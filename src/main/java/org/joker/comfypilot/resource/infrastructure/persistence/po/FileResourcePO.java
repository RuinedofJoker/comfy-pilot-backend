package org.joker.comfypilot.resource.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 文件资源持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("file_resource")
public class FileResourcePO extends BasePO {

    private static final long serialVersionUID = 1L;

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
}
