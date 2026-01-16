package org.joker.comfypilot.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.entity.UserRole;
import org.joker.comfypilot.permission.domain.repository.UserRoleRepository;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.RoleConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.UserRoleConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.mapper.UserRoleMapper;
import org.joker.comfypilot.permission.infrastructure.persistence.po.RolePO;
import org.joker.comfypilot.permission.infrastructure.persistence.po.UserRolePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;
    private final UserRoleConverter userRoleConverter;
    private final RoleConverter roleConverter;

    @Override
    public List<UserRole> findByUserId(Long userId) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getUserId, userId);
        List<UserRolePO> pos = userRoleMapper.selectList(wrapper);
        return pos.stream()
                .map(userRoleConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        List<RolePO> rolePOs = userRoleMapper.findRolesByUserId(userId);
        return rolePOs.stream()
                .map(roleConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public UserRole save(UserRole userRole) {
        UserRolePO po = userRoleConverter.toPO(userRole);
        if (po.getId() == null) {
            userRoleMapper.insert(po);
        } else {
            userRoleMapper.updateById(po);
        }
        return userRoleConverter.toDomain(po);
    }

    @Override
    public void deleteByUserIdAndRoleId(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRolePO::getUserId, userId)
                .eq(UserRolePO::getRoleId, roleId);
        userRoleMapper.delete(wrapper);
    }
}
