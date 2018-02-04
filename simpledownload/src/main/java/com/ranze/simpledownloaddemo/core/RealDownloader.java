package com.ranze.simpledownloaddemo.core;

import com.ranze.simpledownloaddemo.SimpleDownloadClient;
import com.ranze.simpledownloaddemo.core.net.NetWorker;
import com.ranze.simpledownloaddemo.util.HttpUtil;
import com.ranze.simpledownloaddemo.util.IOUtil;
import com.ranze.simpledownloaddemo.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import retrofit2.Response;

/**
 * Created by ranze on 2018/1/22.
 */

public class RealDownloader implements Downloader {
    private Downloader mDownloader;
    private SimpleDownloadClient mClient;
    private Task mTask;
    private DownloadListener mDownloadListener;
    private File mTargetFile;

    public RealDownloader(SimpleDownloadClient client, Task task) {
        this.mClient = client;
        this.mTask = task;
        this.mTargetFile = new File(task.savedPath() + File.separator + task.fileName());
    }

    @Override
    public void start(final DownloadListener listener) {
        mDownloadListener = listener;

        new Thread() {
            @Override
            public void run() {
                super.run();
                Response<Void> response;
                RandomAccessFile raf = null;
                long contentLength = 0;
                try {
                    response = NetWorker.getInstance(mClient.okHttpClient()).checkUrl(mTask.url());


                    boolean success = createTargetFile();
                    if (!success) {
                        throw new IOException("Create target file " + mTargetFile + " failed");
                    }

                    contentLength = HttpUtil.contentLength(response);

                    if (HttpUtil.isSupportRange(response)) {
                        LogUtil.d("This url " + mTask.url() + " support range start");

                        raf = new RandomAccessFile(mTargetFile, "rwd");
                        if (raf.length() != contentLength) {
                            raf.setLength(contentLength);
                        }

                        mDownloader = new RangeDownloader(mClient, mTask, contentLength, mClient.threadCount());
                    } else {
                        LogUtil.d("This url: " + mTask.url() + " doesn't support range download");
                        mDownloader = new NormalDownloader(mClient, mTask, contentLength);
                    }

                    mDownloader.start(listener);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    doError(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    doError(e);
                } finally {
                    IOUtil.close(raf);
                }
            }

        }.start();

    }

    private boolean createTargetFile() throws IOException {
        boolean success = true;
        File fileDir = new File(mTask.savedPath());
        if (!fileDir.exists()) {
            success = fileDir.mkdirs();
        }

        if (!mTargetFile.exists()) {
            success = mTargetFile.createNewFile();
        }
        return success;

    }

    @Override
    public void pause() {
        if (mDownloader != null) {
            mDownloader.pause();
        }
    }

    @Override
    public void cancel() {
        if (mDownloader != null) {
            mDownloader.cancel();
        }
    }

    private void doError(Throwable t) {
        if (mDownloadListener != null) {
            mClient.handler().post(() -> {
                mDownloadListener.onError(t);
            });
        }
    }

}
