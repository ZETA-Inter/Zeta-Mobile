package com.example.core.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.core.R;
import com.example.core.client.ApiPostgresClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile Retrofit retrofit;
    private static volatile ApiPostgresClient api;

    private RetrofitClient() {}

    @NonNull
    public static ApiPostgresClient getApiService(@NonNull Context context) {
        if (api == null) {
            synchronized (RetrofitClient.class) {
                if (api == null) {
                    api = getInstance(context).create(ApiPostgresClient.class);
                }
            }
        }
        return api;
    }

    @NonNull
    public static Retrofit getInstance(@NonNull Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    String baseUrl = ensureSlash(context.getString(R.string.core_api_base_url));
                    final String tokenPref = context.getString(R.string.core_api_token);

                    Log.d("TOKEN_DEBUG", "Token configurado: " + tokenPref);


                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @Override public Response intercept(Chain chain) throws IOException {
                                    Request original = chain.request();
                                    Request.Builder b = original.newBuilder()
                                            .header("Accept", "application/json");
                                    if (tokenPref != null && !tokenPref.trim().isEmpty()) {
                                        b.header("Authorization", "Bearer " + tokenPref.trim());
                                    }

                                    Request request = b.method(original.method(), original.body()).build();
                                    Log.d("Retrofit", "Headers enviados: " + request.headers());
                                    return chain.proceed(request);                                }
                            })
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }

    private static String ensureSlash(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("core_api_base_url vazio. Configure via resValue no build.gradle do :core.");
        }
        String u = url.trim();
        return u.endsWith("/") ? u : (u + "/");
    }

    public static void invalidate() {
        synchronized (RetrofitClient.class) {
            retrofit = null;
            api = null;
        }
    }
}
