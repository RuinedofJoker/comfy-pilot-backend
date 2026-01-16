package org.joker.comfypilot.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.repository.PermissionRepository;
import org.joker.comfypilot.permission.infrastructure.persistence.converter.PermissionConverter;
import org.joker.comfypilot.permission.infrastructure.persistence.mapper.PermissionMapper;
import org.joker.comfypilot.permission.infrastructure.persistence.po.PermissionPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;
    private final PermissionConverter permissionConverter;

    @Override
    public Optional<Permission> findById(Long id) {
        PermissionPO po = permissionMapper.selectById(id);
        return Optional.ofNullable(permissionConverter.toDomain(po));
    }

    @Override
    public Optional<Permission> findByPermissionCode(String permissionCode) {
        LambdaQueryWrapper<PermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionPO::getPermissionCode, permissionCode)
                .eq(PermissionPO::getIsDeleted, false);
        PermissionPO po = permissionMapper.selectOne(wrapper);
        return Optional.ofNullable(permissionConverter.toDomain(po));
    }

    @Override
    public List<Permission> findByIds(List<Long> ids) {
        List<PermissionPO> pos = permissionMapper.selectBatchIds(ids);
        return pos.stream()
                .map(permissionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Permission save(Permission permission) {
        PermissionPO po = permissionConverter.toPO(permission);
        if (po.getId() == null) {
            permissionMapper.insert(po);
        } else {
            permissionMapper.updateById(po);
        }
        return permissionConverter.toDomain(po);
    }
}
