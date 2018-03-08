package com.ranze.simpledownload.core;

import com.ranze.simpledownload.SimpleDownloadClient;
import com.ranze.simpledownload.core.net.NetWorker;
import com.ranze.simpledownload.util.LogUtil;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ranze on 2018/1/24.
 */

abstract class BaseDownloader implements Downloader {
    protected ExecutorService mExecutorService;
    protected SimpleDownloadClient client;
    protected Task mTask;
    protected volatile DownloadStatus mStatus;
    protected File mTargetFile;
    protected NetWorker mNetWorker;
    protected DownloadListener mDownloadListener;
    protected long mContentLength;
    protected AtomicLong mDownloadedSize = new AtomicLong(0);
    protected CountDownLatch mCountDownLatch;


    public BaseDownloader(SimpleDownloadClient client, Task task, CountDownLatch countDownLatch) {
        this.client = client;
        this.mTask = task;
        mStatus = DownloadStatus.preparing;
        mTargetFile = new File(task.savedPath() + File.separator + task.fileName());
        mNetWorker = NetWorker.getInstance(client.okHttpClient());
        mCountDownLatch = countDownLatch;
    }

    @Override
    public void download(DownloadListener listener) {
        mDownloadListener = listener;
        mStatus = DownloadStatus.downloading;
    }

    @Override
    public void pause() {
        mStatus = DownloadStatus.paused;
    }

    @Override
    public void cancel() {
        if (mStatus == DownloadStatus.completed || mStatus == DownloadStatus.canceled) {
            return;
        }

        mStatus = DownloadStatus.canceled;

        executorService().execute(this::deleteFile);
    }

    protected void doProgress(long addedSize) {
        if (mStatus == DownloadStatus.canceled) {
            return;
        }

        long curSize = mDownloadedSize.addAndGet(addedSize);
        if (mDownloadListener != null) {
            client.handler().post(() -> {
                mDownloadListener.onProgress((int) ((curSize * 1.0f / mContentLength) * 100));
                LogUtil.d("ContentLength = " + mContentLength + ", curSize = " + curSize);

            });

            if (curSize == mContentLength) {
                mStatus = DownloadStatus.completed;
                client.handler().post(() -> {
                    mDownloadListener.onComplete();
                });
            }
        }

    }

    protected synchronized ExecutorService executorService() {
        if (mExecutorService == null) {
            mExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory("SimpleDownload Dispatcher#", false));
        }
        return mExecutorService;
    }


    protected void doError(Throwable t) {
        // 如果 status 是 cancel, 则无需处理。
        // 如果 status 已经是 error, 则不要多次调用 onError
        if (mStatus != DownloadStatus.canceled && mStatus != DownloadStatus.error) {
            // 如果某个线程出错，则将 mStatus 置为 error，让其它线程取消下载
            mStatus = DownloadStatus.error;
            if (mDownloadListener != null) {
                client.handler().post(() -> mDownloadListener.onError(t));
            }

        }
    }

    private void deleteFile() {
        if (mTargetFile.exists()) {
            mTargetFile.delete();
        }
    }

    private static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

}
