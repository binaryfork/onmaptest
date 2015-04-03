package com.binaryfork.onmap.instagram;

import android.content.Context;

import com.binaryfork.onmap.Constants;
import com.binaryfork.onmap.instagram.services.MediaService;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class Instagram {
    private static final String API_URL = "https://api.instagram.com/v1/";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String CLIENT_ID = "e116cf2defd74561bef595c78bf23697";
    private static Instagram instance;
    private Context context;

    private RestAdapter restAdapter;
    private OkHttpClient okHttpClient;

    public Instagram(Context context) {
        this.context = context;
    }

    public static Instagram getInstance(Context context) {
        if (instance == null) {
            instance = new Instagram(context);
        }
        return instance;
    }

    private RestAdapter getRestAdapter() {
        if (restAdapter == null) {
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            OkHttpClient client = new OkHttpClient();
            client.setCache(cache);

            RestAdapter.Builder builder = new RestAdapter.Builder();
            builder
                    .setClient(new OkClient(client))
                    .setEndpoint(API_URL)
                    .setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam(PARAM_CLIENT_ID, CLIENT_ID);
                }
            });

            if (Constants.DEBUG) {
                builder.setLogLevel(RestAdapter.LogLevel.FULL);
            }

            if (okHttpClient != null)
                builder.setClient(new OkClient(okHttpClient));

            restAdapter = builder.build();
        }

        return restAdapter;
    }

    public MediaService mediaService() {
        return getRestAdapter().create(MediaService.class);
    }
}