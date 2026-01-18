package org.joker.comfypilot.workflow.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.util.RedisUtil;
import org.joker.comfypilot.workflow.application.service.WorkflowLockService;
import org.joker.comfypilot.workflow.domain.model.WorkflowLockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 工作流锁定服务实现类
 * 使用Redis管理工作流的锁定状态
 */
@Slf4j
@Service
public class WorkflowLockServiceImpl implements WorkflowLockService {

    private static final String LOCK_KEY_PREFIX = "workflow:lock:";
    private static final long LOCK_EXPIRE_TIME = 30; // 锁定过期时间：30分钟
    private static final TimeUnit LOCK_EXPIRE_UNIT = TimeUnit.MINUTES;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 生成Redis锁定键
     */
    private String getLockKey(Long workflowId) {
        return LOCK_KEY_PREFIX + workflowId;
    }

    @Override
    public boolean lockWorkflow(Long workflowId, Long messageId) {
        String lockKey = getLockKey(workflowId);

        // 检查是否已被锁定
        if (redisUtil.hasKey(lockKey)) {
            log.warn("工作流已被锁定, workflowId: {}", workflowId);
            return false;
        }

        // 创建锁定信息
        WorkflowLockInfo lockInfo = WorkflowLockInfo.builder()
                .workflowId(workflowId)
                .messageId(messageId)
                .lockedAt(LocalDateTime.now())
                .build();

        // 存储到Redis，设置过期时间
        boolean success = redisUtil.set(lockKey, lockInfo, LOCK_EXPIRE_TIME, LOCK_EXPIRE_UNIT);

        if (success) {
            log.info("锁定工作流成功, workflowId: {}, messageId: {}", workflowId, messageId);
        } else {
            log.error("锁定工作流失败, workflowId: {}, messageId: {}", workflowId, messageId);
        }

        return success;
    }

    @Override
    public boolean unlockWorkflow(Long workflowId, Long messageId) {
        String lockKey = getLockKey(workflowId);

        // 获取锁定信息
        Optional<WorkflowLockInfo> lockInfoOpt = getLockInfo(workflowId);
        if (lockInfoOpt.isEmpty()) {
            log.warn("工作流未被锁定, workflowId: {}", workflowId);
            return false;
        }

        WorkflowLockInfo lockInfo = lockInfoOpt.get();

        // 检查是否是锁定该工作流的消息
        if (!lockInfo.getMessageId().equals(messageId)) {
            log.warn("只有锁定该工作流的消息才能解锁, workflowId: {}, lockMessageId: {}, requestMessageId: {}",
                    workflowId, lockInfo.getMessageId(), messageId);
            return false;
        }

        // 删除锁定信息
        redisUtil.del(lockKey);
        log.info("解锁工作流成功, workflowId: {}, messageId: {}", workflowId, messageId);

        return true;
    }

    @Override
    public Optional<WorkflowLockInfo> getLockInfo(Long workflowId) {
        String lockKey = getLockKey(workflowId);
        Object lockObj = redisUtil.get(lockKey);

        if (lockObj == null) {
            return Optional.empty();
        }

        if (lockObj instanceof WorkflowLockInfo) {
            return Optional.of((WorkflowLockInfo) lockObj);
        }

        log.error("Redis中的锁定信息类型错误, workflowId: {}, type: {}", workflowId, lockObj.getClass());
        return Optional.empty();
    }

    @Override
    public boolean isLocked(Long workflowId) {
        String lockKey = getLockKey(workflowId);
        return redisUtil.hasKey(lockKey);
    }

    @Override
    public boolean isLockedByMessage(Long workflowId, Long messageId) {
        Optional<WorkflowLockInfo> lockInfoOpt = getLockInfo(workflowId);
        if (lockInfoOpt.isEmpty()) {
            return false;
        }

        return lockInfoOpt.get().getMessageId().equals(messageId);
    }

    @Override
    public void forceUnlock(Long workflowId) {
        String lockKey = getLockKey(workflowId);
        redisUtil.del(lockKey);
        log.info("强制解锁工作流, workflowId: {}", workflowId);
    }
}
