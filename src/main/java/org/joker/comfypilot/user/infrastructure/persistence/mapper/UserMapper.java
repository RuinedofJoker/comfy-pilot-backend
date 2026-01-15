package org.joker.comfypilot.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.user.infrastructure.persistence.po.UserPO;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 根据用户名查询用户
     */
    UserPO selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    UserPO selectByEmail(String email);
}
