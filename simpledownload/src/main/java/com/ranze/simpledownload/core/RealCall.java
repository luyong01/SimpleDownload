package com.ranze.simpledownload.core;

import com.ranze.simpledownload.SimpleDownloadClient;

/**
 * Created by ranze on 2018/3/6.
 */

public class RealCall implements Call {
    private SimpleDownloadClient mClient;
    private Task originTask;
    private boolean mExecuted;
    private boolean mPaused;
    private boolean mCanceled;

    private RealDownloader mRealDownloader;

    public RealCall(SimpleDownloadClient client, Task task) {
        mClient = client;
        this.originTask = task;
        mRealDownloader = new RealDownloader(mClient, originTask);
    }


    @Override
    public Task task() {
        return originTask;
    }

    @Override
    public void enqueue(DownloadListener listener) {
        synchronized (this) {
            if (mExecuted) {
                throw new IllegalStateException("Already started ");
            }
            mExecuted = true;
        }

        mClient.dispatcher().enqueue(new DownloadCall(listener));

    }

    @Override
    public void resume() {
        synchronized (this) {
            if (!mExecuted || !mPaused || mCanceled) {
                throw new IllegalStateException("Current Call is not at paused status");
            }
            mPaused = true;
        }

    }

    @Override
    public void pause() {
        mPaused = true;
        mRealDownloader.pause();
    }

    @Override
    public void cancel() {
        mCanceled = true;
        mRealDownloader.cancel();
    }

    @Override
    public boolean isExecuted() {
        return mExecuted;
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

