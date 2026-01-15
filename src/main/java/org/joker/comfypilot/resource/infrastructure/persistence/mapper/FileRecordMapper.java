package org.joker.comfypilot.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.resource.infrastructure.persistence.po.FileRecordPO;

/**
 * 文件记录Mapper接口
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecordPO> {
}
