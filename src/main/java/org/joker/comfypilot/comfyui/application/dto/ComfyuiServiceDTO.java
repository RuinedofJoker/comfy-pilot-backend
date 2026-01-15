package org.joker.comfypilot.comfyui.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * ComfyUI服务DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComfyuiServiceDTO extends BaseDTO {

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
