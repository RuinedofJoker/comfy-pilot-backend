package org.joker.comfypilot.comfyui.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.joker.comfypilot.comfyui.infrastructure.persistence.po.ComfyuiServicePO;

import java.util.List;

/**
 * ComfyUI服务Mapper接口
 */
@Mapper
public interface ComfyuiServiceMapper extends BaseMapper<ComfyuiServicePO> {

    /**
     * 根据状态查询服务列表
     */
    List<ComfyuiServicePO> selectByStatus(String status);
}
