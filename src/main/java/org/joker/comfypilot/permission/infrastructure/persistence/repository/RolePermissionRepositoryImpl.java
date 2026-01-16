package org.joker.comfypilot.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.entity.RolePermission;
import org.joker.comfypilot.permission.domain.repository.RolePermissionRepository;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.PermissionConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.RolePermissionConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.mapper.RolePermissionMapper;
import org.joker.comfypilot.permission.infrastructure.persistence.po.PermissionPO;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePermissionPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final RolePermissionMapper rolePermissionMapper;
    private final RolePermissionConverter rolePermissionConverter;
    private final PermissionConverter permissionConverter;

    @Override
    public List<RolePermission> findByRoleId(Long roleId) {
        LambdaQueryWrapper<RolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermissionPO::getRoleId, roleId);
        List<RolePermissionPO> pos = rolePermissionMapper.selectList(wrapper);
        return pos.stream()
                .map(rolePermissionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> findPermissionsByRoleId(Long roleId) {
        return findPermissionsByRoleIds(List.of(roleId));
    }

    @Override
    public List<Permission> findPermissionsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<PermissionPO> permissionPOs = rolePermissionMapper.findPermissionsByRoleIds(roleIds);
        return permissionPOs.stream()
                .map(permissionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public RolePermission save(RolePermission rolePermission) {
        RolePermissionPO po = rolePermissionConverter.toPO(rolePermission);
        if (po.getId() == null) {
            rolePermissionMapper.insert(po);
        } else {
            rolePermissionMapper.updateById(po);
        }
        return rolePermissionConverter.toDomain(po);
    }
}
