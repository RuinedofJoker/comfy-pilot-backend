package org.joker.comfypilot.agent.domain.repository;

import org.joker.comfypilot.agent.domain.entity.UserAgentConfig;

import java.util.List;
import java.util.Optional;

/**
 * 用户Agent配置仓储接口
 */
public interface UserAgentConfigRepository {

    /**
     * 保存用户Agent配置
     *
     * @param userAgentConfig 用户Agent配置实体
     * @return 保存后的用户Agent配置实体
     */
    UserAgentConfig saveOrUpdate(UserAgentConfig userAgentConfig);

    /**
     * 根据ID查询用户Agent配置
     *
     * @param id 配置ID
     * @return 用户Agent配置实体
     */
    Optional<UserAgentConfig> findById(Long id);

    /**
     * 根据用户ID和Agent编码查询配置
     *
     * @param userId 用户ID
     * @param agentCode Agent编码
     * @return 用户Agent配置实体
     */
    Optional<UserAgentConfig> findByUserIdAndAgentCode(Long userId, String agentCode);

    /**
     * 根据用户ID查询所有Agent配置
     *
     * @param userId 用户ID
     * @return 用户Agent配置列表
     */
    List<UserAgentConfig> findByUserId(Long userId);

    /**
     * 更新用户Agent配置
     *
     * @param userAgentConfig 用户Agent配置实体
     * @return 更新后的用户Agent配置实体
     */
    UserAgentConfig update(UserAgentConfig userAgentConfig);

    /**
     * 删除用户Agent配置（逻辑删除）
     *
     * @param id 配置ID
     */
    void deleteById(Long id);
}
