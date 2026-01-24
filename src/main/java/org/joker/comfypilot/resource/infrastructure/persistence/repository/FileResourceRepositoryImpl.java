package org.joker.comfypilot.resource.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.resource.domain.entity.FileResource;
import org.joker.comfypilot.resource.domain.enums.FileSourceType;
import org.joker.comfypilot.resource.domain.repository.FileResourceRepository;
import org.joker.comfypilot.resource.infrastructure.persistence.converter.FileResourceConverter;
import org.joker.comfypilot.resource.infrastructure.persistence.mapper.FileResourceMapper;
import org.joker.comfypilot.resource.infrastructure.persistence.po.FileResourcePO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件资源仓储实现
 */
@Repository
public class FileResourceRepositoryImpl implements FileResourceRepository {

    @Autowired
    private FileResourceMapper fileResourceMapper;
    @Autowired
    private FileResourceConverter fileResourceConverter;

    @Override
    public Optional<FileResource> findById(Long id) {
        FileResourcePO po = fileResourceMapper.selectById(id);
        return Optional.ofNullable(fileResourceConverter.toDomain(po));
    }

    @Override
    public Optional<FileResource> findBySourceAndStoredName(String storedName, FileSourceType sourceType) {
        LambdaQueryWrapper<FileResourcePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResourcePO::getSourceType, sourceType.name()).eq(FileResourcePO::getStoredName, storedName);
        FileResourcePO po = fileResourceMapper.selectOne(wrapper);
        return Optional.ofNullable(fileResourceConverter.toDomain(po));
    }

    @Override
    public List<FileResource> findByUploadUserId(Long userId) {
        LambdaQueryWrapper<FileResourcePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResourcePO::getUploadUserId, userId);
        List<FileResourcePO> pos = fileResourceMapper.selectList(wrapper);
        return pos.stream()
                .map(fileResourceConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileResource> findByBusinessInfo(String businessType, Long businessId) {
        LambdaQueryWrapper<FileResourcePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResourcePO::getBusinessType, businessType)
                .eq(FileResourcePO::getBusinessId, businessId);
        List<FileResourcePO> pos = fileResourceMapper.selectList(wrapper);
        return pos.stream()
                .map(fileResourceConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public FileResource save(FileResource fileResource) {
        FileResourcePO po = fileResourceConverter.toPO(fileResource);
        fileResourceMapper.insertOrUpdate(po);
        return fileResourceConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        FileResourcePO po = fileResourceMapper.selectById(id);
        if (po != null) {
            po.setIsDeleted(System.currentTimeMillis());
            fileResourceMapper.updateById(po);
        }
    }
}
