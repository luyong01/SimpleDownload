package com.ranze.simpledownload.core;

import com.ranze.simpledownload.SimpleDownloadClient;

/**
 * Created by ranze on 2018/3/6.
 */

public class RealCall implements Call {
    private SimpleDownloadClient mClient;
    private Task originTask;
    private boolean mExecuting;
    private boolean mPaused;
    private boolean mCanceled;

    private RealDownloader mRealDownloader;

    private DownloadListener mDownloadListener;

    public RealCall(SimpleDownloadClient client, Task task) {
        mClient = client;
        this.originTask = task;
        mRealDownloader = new RealDownloader(mClient, originTask);
    }


    @Override
    public Task task() {
        return originTask;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    @Override
    public void start() {
        synchronized (this) {
            if (mExecuting) {
                throw new IllegalStateException("Call is executing");
            }
            if (isCanceled()) {
                throw new IllegalStateException("Call is canceled");
            }
            mExecuting = true;
            mPaused = false;
        }

        mClient.dispatcher().enqueue(new DownloadCall(mDownloadListener));

    }

    @Override
    public void pause() {
        mPaused = true;
        mExecuting = false;
        mRealDownloader.pause();
    }

    @Override
    public void cancel() {
        mCanceled = true;
        mRealDownloader.cancel();
    }

    @Override
    public boolean isExecuting() {
        return mExecuting;
    }

    @Override
    public boolean isPaused() {
        return mPaused;
    }

    @Override
    public boolean isCanceled() {
        return mCanceled;
    }

    class DownloadCall implements Runnable {
        private DownloadListener listener;

        DownloadCall(DownloadListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                mRealDownloader.download(listener);
            } finally {
                mClient.dispatcher().finished(this);
            }
        }
    }
}

