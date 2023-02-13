package com.anikanov.paper.crawler.source.scholars.api;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;


public class ScholarsHttpClientFactory {

    public static OkHttpClient getPublicClient() {
        return buildHttpClient(null);
    }

    private static OkHttpClient buildHttpClient(Interceptor interceptor) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);
        dispatcher.setMaxRequests(100);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .pingInterval(30, TimeUnit.SECONDS);
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }
}

