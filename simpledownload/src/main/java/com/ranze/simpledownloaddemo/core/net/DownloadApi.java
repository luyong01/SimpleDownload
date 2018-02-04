package com.ranze.simpledownloaddemo.core.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by ranze on 2018/1/14.
 */

interface DownloadApi {
    @GET
    Call<Void> checkUrl(@Header("Range") String range, @Url String url);

    @GET
    @Streaming
    Call<ResponseBody> download(@Header("Range") String range, @Url String url);

    @GET
    @Streaming
    Call<ResponseBody> download(@Url String url);

}
