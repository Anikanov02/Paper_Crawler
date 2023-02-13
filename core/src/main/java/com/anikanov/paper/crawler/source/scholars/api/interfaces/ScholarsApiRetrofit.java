package com.anikanov.paper.crawler.source.scholars.api.interfaces;

import com.anikanov.paper.crawler.source.scholars.api.models.Engine;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsMetadataResponse;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsOrganicSearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ScholarsApiRetrofit {
    @GET("search")
    Call<ScholarsOrganicSearchResponse> getOrganicResults(
            @Query("engine") Engine engine,
            @Query("q") String rows,
            @Query("api_key") String select);

    @GET("search")
    Call<ScholarsMetadataResponse> getMetadata(@Query("engine") Engine engine,
                                               @Query("q") String rows,
                                               @Query("api_key") String select);
}
