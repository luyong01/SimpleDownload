package com.ranze.simpledownloaddemo.core;

/**
 * Created by ranze on 2018/1/22.
 */

public interface Downloader {
    void start(DownloadListener listener);

    void pause();

    void cancel();

}
