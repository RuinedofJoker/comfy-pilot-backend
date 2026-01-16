package org.joker.comfypilot.notification.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.notification.infrastructure.persistence.po.EmailLogPO;

/**
 * 邮件日志Mapper
 */
@Mapper
public interface EmailLogMapper extends BaseMapper<EmailLogPO> {
}
