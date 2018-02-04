package com.ranze.simpledownloaddemo.core;

import com.ranze.simpledownloaddemo.SimpleDownloadClient;
import com.ranze.simpledownloaddemo.core.net.NetWorker;
import com.ranze.simpledownloaddemo.util.LogUtil;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
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
    protected static ExecutorService sExecutorServices;
    protected static Deque<Task> sRunningDeque = new ArrayDeque<>();
    protected SimpleDownloadClient client;
    protected Task mTask;
    protected volatile DownloadStatus mStatus;
    protected File mTargetFile;
    protected NetWorker mNetWorker;
    protected DownloadListener mDownloadListener;
    protected long mContentLength;
    protected AtomicLong mDownloadedSize = new AtomicLong(0);

    public BaseDownloader(SimpleDownloadClient client, Task task) {
        this.client = client;
        this.mTask = task;
        mStatus = DownloadStatus.preparing;
        mTargetFile = new File(task.savedPath() + File.separator + task.fileName());
        mNetWorker = NetWorker.getInstance(client.okHttpClient());
    }

    @Override
    public void start(DownloadListener listener) {
        mDownloadListener = listener;
        mStatus = DownloadStatus.downloading;
        // 去除重复 mTask
        synchronized (BaseDownloader.class) {
            if (!sRunningDeque.contains(mTask)) {
                sRunningDeque.add(mTask);
                if (mDownloadListener != null) {
                    client.handler().post(() -> mDownloadListener.onStart());
                }
            } else {
                LogUtil.d("Task " + mTask + "is already in running queue");
                mStatus = DownloadStatus.canceled;
            }
        }
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
        if (sExecutorServices == null) {
            sExecutorServices = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory("SimpleDownload Dispatcher#", false));
        }
        return sExecutorServices;
    }

    protected synchronized void removeTask() {
        sRunningDeque.remove(mTask);
    }

    protected void doError(Throwable t) {
        // 如果 status 是 cancel, 则无需处理。
        // 如果 status 已经是 error, 则不要多次调用 onError
        if (mStatus != DownloadStatus.canceled && mStatus != DownloadStatus.error) {
            mStatus = DownloadStatus.error;
            // 如果某个线程出错，则将 mStatus 置为 canceled，让其它线程取消下载
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

    public synchronized List<Task> runningTasks() {
        List<Task> result = new ArrayList<>();
        result.addAll(sRunningDeque);

        return Collections.unmodifiableList(result);
    }

    private static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }

}
