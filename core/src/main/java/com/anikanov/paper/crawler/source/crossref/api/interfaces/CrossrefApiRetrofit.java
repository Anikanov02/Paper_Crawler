package com.anikanov.paper.crawler.source.crossref.api.interfaces;

import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefResponse;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CrossrefApiRetrofit {
    @GET("works")
    Call<CrossrefResponse<CrossrefMetadataResponse>> getWorks(
            @Query("query.bibliographic") String searchArg,
            @Query("rows") int rows,
            @Query("select") String select);

    @GET("works/{doi}")
    Call<CrossrefResponse<CrossrefMetadataResponse>> getWork(@Path("doi") String doi);
}
