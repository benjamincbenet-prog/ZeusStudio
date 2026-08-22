package com.zeusstudio;

public class CommandResult {
    public final String output;
    public final int exitCode;

    public CommandResult(String output, int exitCode) {
        this.output = output;
        this.exitCode = exitCode;
    }
}
