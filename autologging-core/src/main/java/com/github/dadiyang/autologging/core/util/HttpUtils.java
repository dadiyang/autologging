package com.github.dadiyang.autologging.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * 用最小依赖发送 HTTP 请求
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class HttpUtils {
    private HttpUtils() {
        throw new UnsupportedOperationException("静态工具类不允许被实例化");
    }

    public static String get(String url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        if (httpURLConnection.getResponseCode() != 200) {
            throw new IOException("Response code is " + httpURLConnection.getResponseCode());
        }
        try (InputStream inputStream = httpURLConnection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8.toString());
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            StringBuilder resultBuffer = new StringBuilder();
            String tempLine = null;
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            return resultBuffer.toString();
        }
    }
}

