package org.joker.comfypilot.cfsvr.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.converter.ComfyuiServerConverter;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.mapper.ComfyuiServerMapper;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.po.ComfyuiServerPO;
import org.joker.comfypilot.common.util.RedisUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ComfyUI服务仓储实现
 * 使用Redis缓存提升查询性能，使用缓存双删保证数据一致性
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ComfyuiServerRepositoryImpl implements ComfyuiServerRepository {

    private final ComfyuiServerMapper mapper;
    private final ComfyuiServerConverter converter;
    private final RedisUtil redisUtil;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_PREFIX = "comfyui:server:";

    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 延迟双删时间（毫秒）
     */
    private static final long DELAY_DELETE_MILLIS = 500;

    @Override
    public Optional<ComfyuiServer> findById(Long id) {
        String cacheKey = CACHE_PREFIX + "id:" + id;

        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof ComfyuiServer) {
            log.debug("从缓存获取ComfyUI服务, id: {}", id);
            return Optional.of((ComfyuiServer) cached);
        }

        // 缓存未命中，查询数据库
        ComfyuiServerPO po = mapper.selectById(id);
        Optional<ComfyuiServer> result = Optional.ofNullable(po).map(converter::toDomain);

        // 写入缓存
        result.ifPresent(server -> {
            redisUtil.set(cacheKey, server, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("缓存ComfyUI服务, id: {}", id);
        });

        return result;
    }

    @Override
    public Optional<ComfyuiServer> findByServerKey(String serverKey) {
        String cacheKey = CACHE_PREFIX + "key:" + serverKey;

        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof ComfyuiServer) {
            log.debug("从缓存获取ComfyUI服务, serverKey: {}", serverKey);
            return Optional.of((ComfyuiServer) cached);
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<ComfyuiServerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComfyuiServerPO::getServerKey, serverKey);
        ComfyuiServerPO po = mapper.selectOne(wrapper);
        Optional<ComfyuiServer> result = Optional.ofNullable(po).map(converter::toDomain);

        // 写入缓存
        result.ifPresent(server -> {
            redisUtil.set(cacheKey, server, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("缓存ComfyUI服务, serverKey: {}", serverKey);
        });

        return result;
    }

    @Override
    public List<ComfyuiServer> findAll() {
        List<ComfyuiServerPO> poList = mapper.selectList(null);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComfyuiServer> findByIsEnabled(Boolean isEnabled) {
        LambdaQueryWrapper<ComfyuiServerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComfyuiServerPO::getIsEnabled, isEnabled);
        List<ComfyuiServerPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ComfyuiServer save(ComfyuiServer server) {
        ComfyuiServerPO po = converter.toPO(server);

        if (server.getId() == null) {
            // 新增操作
            mapper.insert(po);
            log.info("创建ComfyUI服务成功, serverKey: {}", server.getServerKey());
        } else {
            // 更新操作 - 使用缓存双删策略
            String idCacheKey = CACHE_PREFIX + "id:" + server.getId();
            String keyCacheKey = CACHE_PREFIX + "key:" + server.getServerKey();

            // 第一次删除缓存
            redisUtil.del(idCacheKey, keyCacheKey);

            // 更新数据库
            mapper.updateById(po);
            log.info("更新ComfyUI服务成功, id: {}", server.getId());

            // 延迟双删
            redisUtil.delayedDoubleDelete(DELAY_DELETE_MILLIS, idCacheKey, keyCacheKey);
        }

        return converter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        // 先查询服务器信息，获取serverKey用于删除缓存
        ComfyuiServerPO po = mapper.selectById(id);
        if (po != null) {
            String idCacheKey = CACHE_PREFIX + "id:" + id;
            String keyCacheKey = CACHE_PREFIX + "key:" + po.getServerKey();

            // 删除缓存
            redisUtil.del(idCacheKey, keyCacheKey);
        }

        // 删除数据库记录
        mapper.deleteById(id);
        log.info("删除ComfyUI服务成功, id: {}", id);
    }
}
