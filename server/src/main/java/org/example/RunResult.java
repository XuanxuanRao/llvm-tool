package org.example;

import lombok.Data;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
public class RunResult {
    private Integer code;
    private String msg;
    private Integer exitCode;
    private String output;

    public static RunResult success(Integer exitCode, String output) {
        RunResult result = new RunResult();
        result.setCode(1);
        result.setMsg("success");
        result.setExitCode(exitCode);
        result.setOutput(output);
        return result;
    }

    public static RunResult error(String msg) {
        RunResult result = new RunResult();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }

}
