package org.joker.comfypilot.comfyui.application.dto;

import lombok.Data;

/**
 * ComfyUI服务创建请求DTO
 */
@Data
public class ComfyuiServiceCreateRequest {

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
}
