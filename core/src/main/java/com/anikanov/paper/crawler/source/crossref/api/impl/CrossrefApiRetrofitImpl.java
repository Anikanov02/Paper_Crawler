package com.anikanov.paper.crawler.source.crossref.api.impl;

import com.anikanov.paper.crawler.source.crossref.api.CrossrefObjectMapper;
import com.anikanov.paper.crawler.source.crossref.api.CrossrefRetrofitFactory;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class CrossrefApiRetrofitImpl<T> {
    private static final Converter.Factory jacksonConverterFactory = JacksonConverterFactory.create(CrossrefObjectMapper.INSTANCE);

    @SuppressWarnings("unchecked")
    private static final Converter<ResponseBody, CrossrefResponse<?>> errorBodyConverter =
            (Converter<ResponseBody, CrossrefResponse<?>>) jacksonConverterFactory.responseBodyConverter(
                    CrossrefResponse.class, new Annotation[0], null);

    protected String baseUrl;

    protected String apiKey;

    protected String secret;

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
            T t = CrossrefRetrofitFactory.getPublicRetorfit(baseUrl).create(tClass);
            apiImpl = t;
            return t;
        }
    }

    /**
     * Execute a REST call and block until the response is received.
     *
     * @throws IOException On socket related errors.
     */
    public <R> R executeSync(Call<CrossrefResponse<R>> call) throws IOException {
        Response<CrossrefResponse<R>> response = call.execute();
        final CrossrefResponse<R> body = response.body();
        if (response.isSuccessful() && body != null && body.isSuccessful()) {
            return body.getData();
        } else {
            CrossrefResponse<?> errorResponse;
            if (response.isSuccessful()) {
                errorResponse = body;
            } else {
                errorResponse = getErrorResponse(response);
            }

            throw new RuntimeException(String.format("Crossref exception: status [%s], message-type [%s] ", errorResponse.getStatus(), errorResponse.getMessageType()));
        }
    }

    /**
     * Extracts and converts the response error body into an object.
     */
    public CrossrefResponse<?> getErrorResponse(Response<?> response) throws IOException {
        return errorBodyConverter.convert(response.errorBody());
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

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
