package org.joker.comfypilot.test.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.test.application.dto.TestUserDTO;
import org.joker.comfypilot.test.infrastructure.persistence.mapper.TestUserMapper;
import org.joker.comfypilot.test.infrastructure.persistence.po.TestUserPO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用户 Service
 */
@Service
public class TestUserService {

    @Autowired
    private TestUserMapper testUserMapper;

    /**
     * 查询所有用户
     */
    public List<TestUserDTO> listAll() {
        List<TestUserPO> poList = testUserMapper.selectList(new LambdaQueryWrapper<>());
        return poList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询用户
     */
    public TestUserDTO getById(Long id) {
        TestUserPO po = testUserMapper.selectById(id);
        return po != null ? convertToDTO(po) : null;
    }

    /**
     * 创建用户
     */
    public TestUserDTO create(TestUserDTO dto) {
        TestUserPO po = new TestUserPO();
        BeanUtils.copyProperties(dto, po);
        testUserMapper.insert(po);
        return convertToDTO(po);
    }

    /**
     * PO 转 DTO
     */
    private TestUserDTO convertToDTO(TestUserPO po) {
        TestUserDTO dto = new TestUserDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }
}
