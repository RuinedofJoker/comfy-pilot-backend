package org.joker.comfypilot.permission.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePO;
import org.joker.comfypilot.permission.infrastructure.persistence.po.UserRolePO;

import java.util.List;

/**
 * 用户角色Mapper接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {

    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM role r " +
            "INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<RolePO> findRolesByUserId(@Param("userId") Long userId);
}
