package com.ranze.simpledownload.core;

import com.ranze.simpledownload.util.IOUtil;
import com.ranze.simpledownload.SimpleDownloadClient;
import com.ranze.simpledownload.util.LogUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by ranze on 2018/1/22.
 */

class NormalDownloader extends BaseDownloader {
    private InputStream mInputStream;

    NormalDownloader(SimpleDownloadClient client, Task task, long contentLength, CountDownLatch countDownLatch) {
        super(client, task, countDownLatch);
        mContentLength = contentLength;
    }

    @Override
    public void download(DownloadListener listener) {
        super.download(listener);
        if (mStatus != DownloadStatus.downloading) {
            return;
        }
        executorService().execute(() -> {
            Response<ResponseBody> response;
            OutputStream out = null;

            try {
                response = mNetWorker.download("bytes=0-", mTask.url());

                LogUtil.d("get response");

                mInputStream = response.body().byteStream();
                out = new FileOutputStream(mTargetFile);
                byte[] buf = new byte[1024 * 100];
                int len = -1;
                while ((len = mInputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    doProgress(len);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                doError(e);
            } catch (IOException e) {
                e.printStackTrace();
                doError(e);
            } finally {
                IOUtil.close(mInputStream);
                IOUtil.close(out);
                mCountDownLatch.countDown();
            }
        });
    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {
        super.cancel();
    }

}
