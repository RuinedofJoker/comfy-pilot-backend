package org.joker.comfypilot.common.util;

/**
 * 命令执行终端类型枚举
 * 定义了支持的各种 Shell 类型
 */
public enum ShellType {
    /**
     * 自动选择（Windows 优先 PowerShell，Linux/Mac 使用 Bash）
     */
    AUTO,

    /**
     * PowerShell（仅 Windows）
     */
    POWERSHELL,

    /**
     * CMD（仅 Windows）
     */
    CMD,

    /**
     * Bash（Linux/Mac/Windows Git Bash）
     */
    BASH,

    /**
     * Sh（POSIX Shell）
     */
    SH
}
