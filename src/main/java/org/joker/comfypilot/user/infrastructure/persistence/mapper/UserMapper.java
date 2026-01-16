package org.joker.comfypilot.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.user.infrastructure.persistence.po.UserPO;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
