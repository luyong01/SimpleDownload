package com.ranze.simpledownload.core;

/**
 * Created by ranze on 2018/3/6.
 */

public interface Call {
    Task task();

    void enqueue(DownloadListener listener);

    void resume();

    void pause();

    void cancel();

    boolean isExecuted();

    boolean isPaused();

    boolean isCanceled();

    interface Factory {
        Call newCall(Task task);
    }
}
