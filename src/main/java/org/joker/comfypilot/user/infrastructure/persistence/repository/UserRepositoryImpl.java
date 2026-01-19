package org.joker.comfypilot.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.user.domain.entity.User;
import org.joker.comfypilot.user.domain.repository.UserRepository;
import org.joker.comfypilot.user.infrastructure.persistence.converter.UserConverter;
import org.joker.comfypilot.user.infrastructure.persistence.mapper.UserMapper;
import org.joker.comfypilot.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * 用户仓储实现类
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserConverter userConverter;

    @Override
    public Optional<User> findById(Long id) {
        UserPO userPO = userMapper.selectById(id);
        return Optional.ofNullable(userPO)
                .map(userConverter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email);
        UserPO userPO = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userPO)
                .map(userConverter::toDomain);
    }

    @Override
    public Optional<User> findByUserCode(String userCode) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUserCode, userCode);
        UserPO userPO = userMapper.selectOne(wrapper);
        return Optional.ofNullable(userPO)
                .map(userConverter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getEmail, email);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public User save(User user) {
        UserPO userPO = userConverter.toPO(user);
        userMapper.insertOrUpdate(userPO);
        return userConverter.toDomain(userPO);
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }
}
