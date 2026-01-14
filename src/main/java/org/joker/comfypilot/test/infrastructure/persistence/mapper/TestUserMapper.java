package org.joker.comfypilot.test.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.test.infrastructure.persistence.po.TestUserPO;

/**
 * 测试用户 Mapper
 */
@Mapper
public interface TestUserMapper extends BaseMapper<TestUserPO> {
}
