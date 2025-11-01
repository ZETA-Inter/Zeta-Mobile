package com.example.core.cloudinary;

import android.content.Context;

import com.example.core.R;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class CloudinaryClient {
    private static volatile CloudinaryApi api;

    private CloudinaryClient(){}

    public static CloudinaryApi get(Context ctx) {
        if (api == null) {
            synchronized (CloudinaryClient.class) {
                if (api == null) {
                    String cloudName = ctx.getString(R.string.cloudinary_cloud_name);
                    String baseUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/";

                    OkHttpClient ok = new OkHttpClient.Builder().build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(ok)
                            .build();

                    api = retrofit.create(CloudinaryApi.class);
                }
            }
        }
        return api;
    }
}
