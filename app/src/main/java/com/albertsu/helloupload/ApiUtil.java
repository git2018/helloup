package com.albertsu.helloupload;


import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiUtil {

    private static int TIMEOUT = 3000;


    private static HttpURLConnection connect(String url) throws Exception {
        URL mUrl = new URL(url);
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) mUrl.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setDoInput(true);
        return conn;
    }


    private static HttpURLConnection connect(String url, String method, @Nullable String contentType) throws Exception {
        HttpURLConnection conn = connect(url);
        if (method.equals("POST")) {
            conn.setDoOutput(true);
        }
        conn.setRequestMethod(method);
        if (contentType != null) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        return conn;
    }

    private static HttpURLConnection connect(String url, String method, String accessToken, @Nullable String contentType) throws Exception {
        HttpURLConnection conn = connect(url, method, null);
        if (contentType != null) {
            conn.setRequestProperty("Content-Type", contentType);
           conn.setRequestProperty("Connection","close");
// (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
//            conn.setRequestProperty("Content-type", "application/x-java-serialized-object");
        }
        conn.setRequestProperty("Authorization", "Basic " + accessToken);
        return conn;
    }

    /**
     * 使用Authorization验证的get请求
     */
    public static String authHttpGet(String url) {

        String result = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            String source = "4f860649600a47508379639dba82327d:f3b58f6b935b4acd9d9e795864ad10d4";
            String auth = Base64.encodeToString(source.getBytes(), 10);
            connection = connect(url, "GET", auth, null);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }
}
