package com.ranze.simpledownloaddemo.util;

import android.util.Log;

/**
 * Created by ranze on 2018/1/25.
 */

public class LogUtil {
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    private static int logLevel = 5;

    private static final String TAG = "SimpleDownload";

    public static void setLevel(int level) {
        logLevel = level;
    }

    public static void v(String msg) {
        if (logLevel >= VERBOSE) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (logLevel >= DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (logLevel >= INFO) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (logLevel >= WARN) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (logLevel >= ERROR) {
            Log.e(TAG, msg);
        }
    }
}
