package com.ranze.simpledownloaddemo.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by ranze on 2018/1/21.
 */

public class IOUtil {
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
