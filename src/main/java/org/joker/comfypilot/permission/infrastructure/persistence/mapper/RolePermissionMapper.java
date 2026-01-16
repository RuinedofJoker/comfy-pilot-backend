package org.joker.comfypilot.permission.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.joker.comfypilot.permission.infrastructure.persistence.po.PermissionPO;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePermissionPO;

import java.util.List;

/**
 * 角色权限Mapper接口
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionPO> {

    /**
     * 根据角色ID列表查询权限列表
     */
    @Select("<script>" +
            "SELECT DISTINCT p.* FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            " AND p.is_deleted = 0" +
            "</script>")
    List<PermissionPO> findPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
