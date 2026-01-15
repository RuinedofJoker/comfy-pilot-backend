package org.joker.comfypilot.comfyui.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * ComfyUI服务持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comfyui_service")
public class ComfyuiServicePO extends BasePO {

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务URL
     */
    private String url;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * 最后检测时间
     */
    private LocalDateTime lastCheckTime;
}
