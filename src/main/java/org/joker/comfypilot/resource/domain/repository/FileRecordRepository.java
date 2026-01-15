package org.joker.comfypilot.resource.domain.repository;

import org.joker.comfypilot.resource.domain.entity.FileRecord;
import org.joker.comfypilot.resource.domain.enums.BusinessType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件记录仓储接口
 */
public interface FileRecordRepository {

    /**
     * 保存文件记录
     */
    FileRecord save(FileRecord fileRecord);

    /**
     * 根据ID查询
     */
    Optional<FileRecord> findById(Long id);

    /**
     * 根据用户ID查询
     */
    List<FileRecord> findByUserId(Long userId);

    /**
     * 根据业务类型查询
     */
    List<FileRecord> findByBusinessType(BusinessType businessType);

    /**
     * 查询过期文件
     */
    List<FileRecord> findExpiredFiles(LocalDateTime now);

    /**
     * 删除文件记录
     */
    void deleteById(Long id);
}
