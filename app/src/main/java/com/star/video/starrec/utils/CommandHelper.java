package com.star.video.starrec.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.Context;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;


public class CommandHelper {
    // default time out, in millseconds
    public static int DEFAULT_TIMEOUT;
    public static final int DEFAULT_INTERVAL = 1000;
    public static long START;
    public static final String READ_CID_COMMAND = "cat /sys/bus/mmc/devices/mmc0:0001/cid";
    public static final String TAG = "CommandHelper";
    public static final String REGEX_CID = "[0-9A-Za-z]*";

    private static TelephonyManager telephonyManager = null;
    /**
     * 众鸿sn号写入的头部信息 "SN : "
     */
    private final static String SN_HEAD = "SN : ";

    public static CommandResult exec(String command) throws IOException,
            InterruptedException {
        Process process = Runtime.getRuntime().exec(command);// 创建一个字进程，并保存在process对象中

        CommandResult commandResult = wait(process);

        if (process != null) {
            process.destroy();
        }
        return commandResult;
    }

    private static boolean isOverTime() {
        return System.currentTimeMillis() - START >= DEFAULT_TIMEOUT;
    }

    private static CommandResult wait(Process process)
            throws InterruptedException, IOException {
        BufferedReader errorStreamReader = null;
        BufferedReader inputStreamReader = null;
        try {
            errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // timeout control
            START = System.currentTimeMillis();
            boolean isFinished = false;
            for (;;) {
                if (isOverTime()) {
                    CommandResult result = new CommandResult();
                    result.setExitValue(CommandResult.EXIT_VALUE_TIMEOUT);
                    result.setOutput("Command process timeout");
                    return result;
                }
                if (isFinished) {
                    CommandResult result = new CommandResult();
                    result.setExitValue(process.waitFor()); // process.waitFor()
                    // 表示 等这条语句执行完后再往下执行
                    // parse error info
                    if (errorStreamReader.ready()) {
                        StringBuilder buffer = new StringBuilder();
                        String line;
                        while ((line = errorStreamReader.readLine()) != null) {
                            buffer.append(line);
                        }
                        result.setError(buffer.toString());
                    }
                    // parse info
                    if (inputStreamReader.ready()) {
                        StringBuilder buffer = new StringBuilder();
                        String line;
                        while ((line = inputStreamReader.readLine()) != null) {
                            buffer.append(line);
                        }
                        result.setOutput(buffer.toString());
                    }
                    return result;
                }
                try {
                    isFinished = true;
                    process.exitValue();
                } catch (IllegalThreadStateException e) {
                    // process hasn't finished yet
                    isFinished = false;
                    Thread.sleep(DEFAULT_INTERVAL);
                }
            }
        } finally {
            if (errorStreamReader != null) {
                try {
                    errorStreamReader.close();
                } catch (IOException e) {
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 读取系统flash标识(mmc cid) 已知786和PX2机器flash目录均为"mmc0:0001",通过命令 cat
     * /sys/bus/mmc/devices/mmc0:0001/cid读取的是32位唯一标识 其他型号机器mmc目录若不同作相应更改。
     */
    public static String getMMCId() throws RemoteException {
        // String cmd = "cat /sys/bus/mmc/devices/mmc0:e118/cid";
        String reply = "";

        Log.v("cmd", READ_CID_COMMAND);
        try {
            CommandHelper.DEFAULT_TIMEOUT = 5000;
            CommandResult result = CommandHelper.exec(READ_CID_COMMAND);
            if (result != null) {
                if (result.getError() != null) {
                    Log.e(TAG, "Error:" + result.getError());
                    reply = result.getError();
                }
                if (result.getOutput() != null) {
                    Log.e(TAG, "Output:" + result.getOutput());
                    reply = result.getOutput();
                }
            }

        } catch (IOException ex) {
            Log.e(TAG, "IOException:" + ex.getLocalizedMessage());
        } catch (InterruptedException ex) {
            Log.e(TAG, "InterruptedException:" + ex.getLocalizedMessage());
        }
        if (!reply.matches(REGEX_CID)) {
            reply = "000000000";// 测试
            //reply = null; // 获取ID异常
        }
        return reply;
    }


}
