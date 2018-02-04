package com.ranze.simpledownloaddemo.util;

import android.text.TextUtils;

import okhttp3.internal.http.HttpHeaders;
import retrofit2.Response;

/**
 * Created by ranze on 2018/1/15.
 */

public class HttpUtil {
    public static boolean isSupportRange(Response<?> response) {
        return response.isSuccessful() && (response.code() == 206 || !TextUtils.isEmpty(
                contentRange(response)) || !TextUtils.isEmpty(acceptRange(response)));
    }

    public static long contentLength(Response<?> response) {
        return HttpHeaders.contentLength(response.headers());
    }

    private static String contentRange(Response<?> response) {
        String header = response.headers().get("Content-Range");
        return header != null ? header : "";
    }

    private static String acceptRange(Response<?> response) {
        String header = response.headers().get("Accept-Ranges");
        return header != null ? header : "";
    }
}
