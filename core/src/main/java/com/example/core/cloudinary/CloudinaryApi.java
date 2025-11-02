package com.example.core.cloudinary;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface CloudinaryApi {
    @Multipart
    @POST("image/upload")
    Call<CloudinaryUploadResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("upload_preset") RequestBody uploadPreset
    );
}
