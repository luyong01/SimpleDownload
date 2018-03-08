package com.ranze.simpledownload.core;

/**
 * Created by ranze on 2018/3/6.
 */

public interface Call {
    Task task();

    void setDownloadListener(DownloadListener downloadListener);

    void start();

    void pause();

    void cancel();

    boolean isExecuting();

    boolean isPaused();

    boolean isCanceled();

    interface Factory {
        Call newCall(Task task);
    }
}
