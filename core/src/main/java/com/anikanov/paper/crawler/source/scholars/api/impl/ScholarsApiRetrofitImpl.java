package com.anikanov.paper.crawler.source.scholars.api.impl;

import com.anikanov.paper.crawler.source.scholars.api.ScholarsObjectMapper;
import com.anikanov.paper.crawler.source.scholars.api.ScholarsRetrofitFactory;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class ScholarsApiRetrofitImpl<T> {
    private static final Converter.Factory jacksonConverterFactory = JacksonConverterFactory.create(ScholarsObjectMapper.INSTANCE);

    protected String baseUrl;

    protected String apiKey;

    private T apiImpl;

    public T getAPIImpl() {
        if (Objects.nonNull(apiImpl))
            return apiImpl;
        synchronized (getClass()) {
            if (Objects.nonNull(apiImpl))
                return apiImpl;
            @SuppressWarnings("unchecked")
            Class<T> tClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
            T t = ScholarsRetrofitFactory.getPublicRetorfit(baseUrl).create(tClass);
            apiImpl = t;
            return t;
        }
    }

    /**
     * Execute a REST call and block until the response is received.
     *
     * @throws IOException On socket related errors.
     */
    public <R> R executeSync(Call<R> call) throws IOException {
        Response<R> response = call.execute();
        final R body = response.body();
        if (response.isSuccessful() && body != null) {
            return body;
        } else {
            throw new RuntimeException(String.format("Scholars exception: code [%s]", response.code()));
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
