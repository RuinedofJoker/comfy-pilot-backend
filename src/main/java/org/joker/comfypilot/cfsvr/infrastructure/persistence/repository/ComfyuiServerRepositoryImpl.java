package org.joker.comfypilot.cfsvr.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.converter.ComfyuiServerConverter;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.mapper.ComfyuiServerMapper;
import org.joker.comfypilot.cfsvr.infrastructure.persistence.po.ComfyuiServerPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ComfyUI服务仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ComfyuiServerRepositoryImpl implements ComfyuiServerRepository {

    private final ComfyuiServerMapper mapper;
    private final ComfyuiServerConverter converter;

    @Override
    public Optional<ComfyuiServer> findById(Long id) {
        ComfyuiServerPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<ComfyuiServer> findByServerKey(String serverKey) {
        LambdaQueryWrapper<ComfyuiServerPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComfyuiServerPO::getServerKey, serverKey);
        ComfyuiServerPO po = mapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(converter::toDomain);
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
            mapper.insert(po);
            log.info("创建ComfyUI服务成功, serverKey: {}", server.getServerKey());
        } else {
            mapper.updateById(po);
            log.info("更新ComfyUI服务成功, id: {}", server.getId());
        }
        return converter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
        log.info("删除ComfyUI服务成功, id: {}", id);
    }
}
