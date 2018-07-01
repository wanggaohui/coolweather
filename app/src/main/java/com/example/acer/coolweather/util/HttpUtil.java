package com.example.acer.coolweather.util;

import java.net.URL;

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
