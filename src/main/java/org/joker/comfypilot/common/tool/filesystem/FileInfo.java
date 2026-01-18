package org.joker.comfypilot.common.tool.filesystem;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

import java.io.Serializable;

/**
 * 文件信息 DTO
 * 用于描述文件或目录的基本信息
 */
@Description("文件或目录的详细信息，包含名称、路径、类型、大小和修改时间")
public record FileInfo(
        @JsonProperty(value = "name", required = true)
        @Description("文件或目录的名称（不包含路径）")
        String name,

        @JsonProperty(value = "path", required = true)
        @Description("文件或目录的完整绝对路径")
        String path,

        @JsonProperty(value = "isDirectory", required = true)
        @Description("标识是否为目录，true 表示目录，false 表示文件")
        Boolean isDirectory,

        @JsonProperty(value = "size", required = true)
        @Description("文件大小，单位为字节，目录的大小为 0")
        Long size,

        @JsonProperty(value = "lastModified", required = true)
        @Description("文件或目录的最后修改时间，Unix 时间戳（毫秒）")
        Long lastModified
) implements Serializable {

    /**
     * 将文件信息转换为 JSON 字符串
     *
     * @return JSON 格式的字符串
     */
    public String toJsonString() {
        return String.format(
                "{\"name\":\"%s\",\"path\":\"%s\",\"isDirectory\":%b,\"size\":%d,\"lastModified\":%d}",
                escapeJson(name),
                escapeJson(path),
                isDirectory != null ? isDirectory : false,
                size != null ? size : 0L,
                lastModified != null ? lastModified : 0L
        );
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

