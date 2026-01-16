package org.joker.comfypilot.user.domain.repository;

import org.joker.comfypilot.user.domain.entity.User;

import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    Optional<User> findById(Long id);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户编码查询用户
     *
     * @param userCode 用户编码
     * @return 用户实体
     */
    Optional<User> findByUserCode(String userCode);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    boolean existsByEmail(String email);

    /**
     * 保存用户（新增或更新）
     *
     * @param user 用户实体
     * @return 保存后的用户实体
     */
    User save(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);
}
