package com.anikanov.paper.crawler.source.crossref.api;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CrossrefRetrofitFactory {
    private static final Converter.Factory CONVERTER_FACTORY = JacksonConverterFactory.create(CrossrefObjectMapper.INSTANCE);

    public static Retrofit getPublicRetorfit(String baseUrl) {

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(CONVERTER_FACTORY)
                .client(CrossrefHttpClientFactory.getPublicClient())
                .build();

    }
}
