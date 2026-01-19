package org.joker.comfypilot.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.repository.RoleRepository;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.RoleConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.mapper.RoleMapper;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * 角色仓储实现
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RoleConverter roleConverter;

    @Override
    public Optional<Role> findById(Long id) {
        RolePO po = roleMapper.selectById(id);
        return Optional.ofNullable(roleConverter.toDomain(po));
    }

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        LambdaQueryWrapper<RolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePO::getRoleCode, roleCode);
        RolePO po = roleMapper.selectOne(wrapper);
        return Optional.ofNullable(roleConverter.toDomain(po));
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        LambdaQueryWrapper<RolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePO::getRoleCode, roleCode);
        return roleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Role save(Role role) {
        RolePO po = roleConverter.toPO(role);
        roleMapper.insertOrUpdate(po);
        return roleConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        RolePO po = roleMapper.selectById(id);
        if (po != null) {
            po.setIsDeleted(System.currentTimeMillis());
            roleMapper.updateById(po);
        }
    }
}
