package com.ranze.simpledownloaddemo.core;

import android.support.annotation.WorkerThread;

import com.ranze.simpledownloaddemo.SimpleDownloadClient;
import com.ranze.simpledownloaddemo.repository.Repository;
import com.ranze.simpledownloaddemo.util.IOUtil;
import com.ranze.simpledownloaddemo.util.LogUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by ranze on 2018/1/19.
 */

class RangeDownloader extends BaseDownloader {
    private AtomicInteger mPausedThreadCount = new AtomicInteger(0);
    private AtomicInteger mEndedThreadCount = new AtomicInteger(0);
    private int mThreadCount;
    private DownloadWorker mDownloadWorkers[];
    private List<Segment> mSegments;
    private Repository mRepository;


    RangeDownloader(SimpleDownloadClient client, Task task, long contentLength, int threadCount) {
        super(client, task);

        if (threadCount < 1) {
            threadCount = 1;
        }
        this.mContentLength = contentLength;

        this.mThreadCount = threadCount;

        this.mDownloadWorkers = new DownloadWorker[threadCount];

        this.mRepository = Repository.getInstance(client.context());
    }

    @WorkerThread
    public void start(DownloadListener listener) {
        super.start(listener);

        if (mStatus != DownloadStatus.downloading) {
            return;
        }

        mSegments = mRepository.loadSegment(mTask.url(), true);
        if (mSegments == null || mSegments.size() == 0) {
            LogUtil.d("Segments is empty in database");
            mSegments = new ArrayList<>();
            long perRange = mContentLength / mThreadCount;
            for (int i = 0; i < mThreadCount; ++i) {
                long startPos = i * perRange;
                long endPos;
                if (i != mThreadCount - 1) {
                    endPos = i * perRange + perRange - 1;
                } else {
                    endPos = mContentLength - 1;
                }
                Segment segment = new Segment();
                segment.tag = mTask.tag() + i;
                segment.url = mTask.url();
                segment.startPos = startPos;
                segment.endPos = endPos;
                segment.writePos = startPos;
                segment.filePath = mTask.savedPath();
                segment.fileName = mTask.fileName();
                mSegments.add(segment);
            }
        }

        LogUtil.d("Segments = " + mSegments);

        for (int i = 0; i < mSegments.size(); ++i) {
            DownloadWorker downloadWorker = new DownloadWorker(mSegments.get(i));
            mDownloadWorkers[i] = downloadWorker;
            mDownloadedSize.addAndGet(mSegments.get(i).writePos - mSegments.get(i).startPos);
        }

        doProgress(0);

        for (DownloadWorker downloadWorker : mDownloadWorkers) {
            executorService().execute(downloadWorker);
        }

    }

    @Override
    public void pause() {
        super.pause();
    }

    public void cancel() {
        super.cancel();
        executorService().execute(this::doCancel);
    }

    @WorkerThread
    private void doCancel() {
        mRepository.deleteSegment(mSegments);
    }

    private void doPause() {

    }

    private void checkForRemoveTask() {
        if (mEndedThreadCount.addAndGet(1) == mThreadCount) {
            removeTask();
        }
    }

    class DownloadWorker implements Runnable {
        private Segment segment;
        private long readSize = 0;
        private InputStream in;

        DownloadWorker(Segment segment) {
            this.segment = segment;
        }

        @Override
        public void run() {
            Response<ResponseBody> response = null;
            try {
                response = mNetWorker.download("bytes=" + segment.writePos + "-" + segment.endPos,
                        segment.url);
                LogUtil.d("Downloading: " + segment.toString());
            } catch (IOException e) {
                e.printStackTrace();
                if (mDownloadListener != null) {
                    client.handler().post(() -> mDownloadListener.onError(e));
                }
            }

            RandomAccessFile raf = null;
            try {
                ResponseBody body = response.body();
                if (body != null) {
                    in = body.byteStream();
                } else {
                    throw new RuntimeException("response.body() return null");
                }

                raf = new RandomAccessFile(mTargetFile, "rwd");
                raf.seek(segment.writePos);
                byte[] buffer = new byte[1024 * 100];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {

                    if (mStatus == DownloadStatus.paused) {
                        segment.writePos += readSize;
                        mRepository.saveSegment(segment);
                        doPause();
                        LogUtil.d("Task " + mTask + "in thread " + Thread.currentThread().getName()
                                + " paused, segments has been saved");
                        break;
                    }
                    if (mStatus == DownloadStatus.canceled) {
                        LogUtil.d("Task " + mTask + " canceled");
                        break;
                    }
                    if (mStatus == DownloadStatus.error) {
                        LogUtil.d("Some other thread occurred error, stop this thread");
                        break;
                    }


                    raf.write(buffer, 0, len);
                    readSize += len;
                    doProgress(len);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                doError(e);
            } catch (IOException e) {
                e.printStackTrace();
                doError(e);
            } finally {
                IOUtil.close(raf);
                IOUtil.close(in);
                checkForRemoveTask();
            }
        }
    }

}
