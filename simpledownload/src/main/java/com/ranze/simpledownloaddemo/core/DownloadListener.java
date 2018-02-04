package com.ranze.simpledownloaddemo.core;

/**
 * Created by ranze on 2018/1/18.
 */

public interface DownloadListener {
    void onStart();

    void onPause();

    void onComplete();

    void onError(Throwable t);

    void onProgress(int progress);

}
