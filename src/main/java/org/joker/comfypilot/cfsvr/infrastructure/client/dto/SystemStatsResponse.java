package org.joker.comfypilot.cfsvr.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ComfyUI系统状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 系统信息
     */
    private SystemInfo system;

    /**
     * 设备信息列表
     */
    private List<DeviceInfo> devices;

    /**
     * 系统信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 操作系统
         */
        private String os;

        /**
         * 总内存（字节）
         */
        @JsonProperty("ram_total")
        private Long ramTotal;

        /**
         * 可用内存（字节）
         */
        @JsonProperty("ram_free")
        private Long ramFree;

        /**
         * ComfyUI版本
         */
        @JsonProperty("comfyui_version")
        private String comfyuiVersion;

        /**
         * Python版本
         */
        @JsonProperty("python_version")
        private String pythonVersion;

        /**
         * PyTorch版本
         */
        @JsonProperty("pytorch_version")
        private String pytorchVersion;

        /**
         * 是否使用嵌入式Python
         */
        @JsonProperty("embedded_python")
        private Boolean embeddedPython;
    }

    /**
     * 设备信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 设备名称
         */
        private String name;

        /**
         * 设备类型（cuda/cpu等）
         */
        private String type;

        /**
         * 设备索引
         */
        private Integer index;

        /**
         * 总显存（字节）
         */
        @JsonProperty("vram_total")
        private Long vramTotal;

        /**
         * 可用显存（字节）
         */
        @JsonProperty("vram_free")
        private Long vramFree;

        /**
         * Torch总显存（字节）
         */
        @JsonProperty("torch_vram_total")
        private Long torchVramTotal;

        /**
         * Torch可用显存（字节）
         */
        @JsonProperty("torch_vram_free")
        private Long torchVramFree;
    }
}
