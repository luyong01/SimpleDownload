package com.ranze.simpledownload.core;

/**
 * Created by ranze on 2018/1/22.
 */

public interface Downloader {
    void download(DownloadListener listener);

    void pause();

    void cancel();

}
