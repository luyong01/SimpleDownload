package com.ranze.simpledownloaddemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ranze.simpledownloaddemo.core.Downloader;
import com.ranze.simpledownloaddemo.core.RealDownloader;
import com.ranze.simpledownloaddemo.core.Task;
import com.ranze.simpledownloaddemo.util.LogUtil;

import okhttp3.OkHttpClient;

/**
 * Created by ranze on 2018/1/18.
 */

public class SimpleDownloadClient {
    private int mThreadCount;
    private Handler mHandler;
    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private boolean mDebuggable;

    private SimpleDownloadClient(Builder builder) {
        this.mThreadCount = builder.threadCount;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mContext = builder.context;
        this.mOkHttpClient = builder.okhttpClient;
        this.mDebuggable = builder.debug;
        if (mDebuggable) {
            LogUtil.setLevel(LogUtil.DEBUG);
        }
    }

    public Context context() {
        return mContext;
    }

    public int threadCount() {
        return mThreadCount;
    }

    public Handler handler() {
        return mHandler;
    }

    public OkHttpClient okHttpClient() {
        return mOkHttpClient;
    }

    public Downloader newDownloader(final Task task) {
        return new RealDownloader(this, task);
    }

    public static class Builder {
        private int threadCount = 3;
        private Context context;
        private OkHttpClient okhttpClient;
        private boolean debug;

        public SimpleDownloadClient build() {
            return new SimpleDownloadClient(this);
        }

        public Builder debuggable(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder context(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        public Builder client(OkHttpClient client) {
            this.okhttpClient = client;
            return this;
        }
    }
}


