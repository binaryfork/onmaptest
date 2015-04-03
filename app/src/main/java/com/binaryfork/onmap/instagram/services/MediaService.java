package com.binaryfork.onmap.instagram.services;


import com.binaryfork.onmap.instagram.model.MediaResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MediaService {

    @GET("/media/{media_id}")
    public MediaResponse media(
            @Path("media_id") String mediaId);

    @GET("/media/search")
    public MediaResponse mediaSearch(
            @Query("lat") double latitude,
            @Query("lng") double longitude);

    @GET("/media/search")
    void mediaSearch(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            Callback<MediaResponse> mapp);
}
