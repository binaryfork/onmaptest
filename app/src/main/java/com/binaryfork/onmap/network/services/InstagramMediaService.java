package com.binaryfork.onmap.network.services;


import com.binaryfork.onmap.network.model.MediaResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface InstagramMediaService {

    @GET("/media/{media_id}")
    MediaResponse media(
            @Path("media_id") String mediaId);

    @GET("/media/search")
    rx.Observable<MediaResponse> mediaSearch(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("min_timestamp") long minTimestamp,
            @Query("max_timestamp") long maxTimestamp,
            @Query("count") int count);

    @GET("/media/search")
    rx.Observable<MediaResponse> mediaSearch(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("count") int count);
}
