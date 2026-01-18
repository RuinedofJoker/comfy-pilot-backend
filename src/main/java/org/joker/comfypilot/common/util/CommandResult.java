package org.joker.comfypilot.common.util;

/**
 * 命令执行结果
 * 封装命令执行的退出码、标准输出和错误输出
 */
public class CommandResult {
    /**
     * 退出码（0 表示成功）
     */
    private final int exitCode;

    /**
     * 标准输出内容
     */
    private final String output;

    /**
     * 错误输出内容
     */
    private final String error;

    public CommandResult(int exitCode, String output, String error) {
        this.exitCode = exitCode;
        this.output = output;
        this.error = error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    /**
     * 判断命令是否执行成功
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "exitCode=" + exitCode +
                ", output='" + output + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
