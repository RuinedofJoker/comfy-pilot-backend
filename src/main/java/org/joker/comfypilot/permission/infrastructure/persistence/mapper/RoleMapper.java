package org.joker.comfypilot.permission.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePO;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {
}
