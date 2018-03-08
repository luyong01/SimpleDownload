package com.ranze.simpledownload.core;

import com.ranze.simpledownload.core.RealCall.DownloadCall;
import com.ranze.simpledownload.util.LogUtil;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ranze on 2018/3/6.
 */

public class Dispatcher {
    private int mMaxTask = 3;
    private ExecutorService mExecutorService;

    private Deque<DownloadCall> mRunningCalls = new ArrayDeque<>();
    private Deque<DownloadCall> mReadyCalls = new ArrayDeque<>();

    public Dispatcher() {

    }

    public void enqueue(DownloadCall downloadCall) {
        if (mRunningCalls.size() < mMaxTask) {
            mRunningCalls.add(downloadCall);
            executorService().execute(downloadCall);
        } else {
            mReadyCalls.add(downloadCall);
        }

        LogUtil.d("enqueue, Running calls: " + mRunningCalls);
        LogUtil.d("enqueue, Ready calls: " + mReadyCalls);
    }

    public void finished(DownloadCall call) {
        synchronized (this) {
            if (!mRunningCalls.remove(call)) {
                throw new AssertionError("Call wasn't in-flight");
            }
            promoteCalls();
        }

        LogUtil.d("finished, Running calls: " + mRunningCalls);
        LogUtil.d("finished, Ready calls: " + mReadyCalls);
    }

    private void promoteCalls() {
        if (mRunningCalls.size() >= mMaxTask) {
            return;
        }

        if (mReadyCalls.isEmpty()) {
            return;
        }

        for (Iterator<DownloadCall> i = mReadyCalls.iterator(); i.hasNext(); ) {
            DownloadCall call = i.next();
            i.remove();
            mRunningCalls.add(call);

            executorService().execute(call);

            if (mRunningCalls.size() >= mMaxTask) {
                return;
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

    private static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread result = new Thread(runnable, name);
            result.setDaemon(daemon);
            return result;
        };
    }


}
