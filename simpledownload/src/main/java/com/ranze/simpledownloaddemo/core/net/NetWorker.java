package com.ranze.simpledownloaddemo.core.net;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by ranze on 2018/1/15.
 */

public class NetWorker {
    private static final String baseUrl = "http://www.example.com/";

    private DownloadApi downloadApi;

    private volatile static NetWorker sInstance;

    @NonNull
    public static NetWorker getInstance(OkHttpClient client) {
        if (sInstance == null) {
            synchronized (NetWorker.class) {
                if (sInstance == null) {
                    sInstance = new NetWorker(client);
                }
            }
        }
        return sInstance;
    }

    private NetWorker(OkHttpClient client) {
        if (client == null) {
            client = new OkHttpClient.Builder().build();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .build();
        downloadApi = retrofit.create(DownloadApi.class);
    }

    public Response<Void> checkUrl(String url) throws IOException {
        Call<Void> checkUrlCall = downloadApi.checkUrl("bytes=0-", url);
        downloadApi.checkUrl("bytes=0-", url);
        Response<Void> response = null;
        response = checkUrlCall.execute();

        return response;
    }

    public Response<ResponseBody> download(String range, String url) throws IOException {
        Call<ResponseBody> downloadCall;
        if (!TextUtils.isEmpty(range)) {
            downloadCall = downloadApi.download(range, url);
        } else {
            downloadCall = downloadApi.download(url);
        }

        Response<ResponseBody> response = null;
        response = downloadCall.execute();

        return response;
    }

}