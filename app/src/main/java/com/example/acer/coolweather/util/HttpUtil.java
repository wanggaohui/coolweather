package com.example.acer.coolweather.util;

import android.app.DownloadManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
//网络请求响应工具类
public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
