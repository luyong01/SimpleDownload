package com.ranze.simpledownload.core;

import com.ranze.simpledownload.SimpleDownloadClient;
import com.ranze.simpledownload.core.net.NetWorker;
import com.ranze.simpledownload.util.HttpUtil;
import com.ranze.simpledownload.util.IOUtil;
import com.ranze.simpledownload.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

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
    private volatile DownloadStatus mStatus = DownloadStatus.preparing;
    private CountDownLatch mCountDownLatch;


    public RealDownloader(SimpleDownloadClient client, Task task) {
        this.mClient = client;
        this.mTask = task;
        this.mTargetFile = new File(task.savedPath() + File.separator + task.fileName());
    }

    @Override
    public void download(final DownloadListener listener) {
        mStatus = DownloadStatus.downloading;
        mDownloadListener = listener;

        Response<Void> response;
        RandomAccessFile raf = null;
        long contentLength = 0;
        try {
            response = NetWorker.getInstance(mClient.okHttpClient()).checkUrl(mTask.url());

            // 获取 response 可能需要较长的时间，在这个过程中 status 可能发生了变化
            if (mStatus != DownloadStatus.downloading) {
                return;
            }

            boolean success = createTargetFile();
            if (!success) {
                throw new IOException("Create target file " + mTargetFile + " failed");
            }

            contentLength = HttpUtil.contentLength(response);

            if (HttpUtil.isSupportRange(response)) {
                LogUtil.d("This url " + mTask.url() + " support range download");

                raf = new RandomAccessFile(mTargetFile, "rwd");
                if (raf.length() != contentLength) {
                    raf.setLength(contentLength);
                }

                mCountDownLatch = new CountDownLatch(mClient.threadCount());
                mDownloader = new RangeDownloader(mClient, mTask, contentLength, mClient.threadCount(), mCountDownLatch);
            } else {
                LogUtil.d("This url: " + mTask.url() + " doesn't support range download");
                mDownloader = new NormalDownloader(mClient, mTask, contentLength, mCountDownLatch);
                mCountDownLatch = new CountDownLatch(1);
            }
            mDownloader.download(listener);
            mCountDownLatch.await();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            doError(e);
        } catch (IOException e) {
            e.printStackTrace();
            doError(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            doError(e);
        } finally {
            IOUtil.close(raf);
        }

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
        mStatus = DownloadStatus.paused;
        if (mDownloader != null) {
            mDownloader.pause();
        }
    }

    @Override
    public void cancel() {
        mStatus = DownloadStatus.canceled;
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
