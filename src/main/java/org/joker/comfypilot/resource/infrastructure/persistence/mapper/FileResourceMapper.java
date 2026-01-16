package org.joker.comfypilot.resource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.resource.infrastructure.persistence.po.FileResourcePO;

/**
 * 文件资源Mapper
 */
@Mapper
public interface FileResourceMapper extends BaseMapper<FileResourcePO> {
}
