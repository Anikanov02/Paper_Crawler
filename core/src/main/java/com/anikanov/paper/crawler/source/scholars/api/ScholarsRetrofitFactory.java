package com.anikanov.paper.crawler.source.scholars.api;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ScholarsRetrofitFactory {
    private static final Converter.Factory CONVERTER_FACTORY = JacksonConverterFactory.create(ScholarsObjectMapper.INSTANCE);

    public static Retrofit getPublicRetorfit(String baseUrl) {

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(CONVERTER_FACTORY)
                .client(ScholarsHttpClientFactory.getPublicClient())
                .build();

    }
}
