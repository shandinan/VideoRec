package com.star.video.starrec.utils;

public class CommandResult {
    public static final int EXIT_VALUE_TIMEOUT = -1;

    private String output;

    public int exitValue;

    private String error;

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
