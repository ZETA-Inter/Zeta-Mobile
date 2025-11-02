package com.example.core.cloudinary;

import com.google.gson.annotations.SerializedName;

public class CloudinaryUploadResponse {
    @SerializedName("public_id") public String publicId;
    @SerializedName("secure_url") public String secureUrl;
    @SerializedName("url") public String url;
    @SerializedName("format") public String format;
    @SerializedName("resource_type") public String resourceType;
    // adicione outros campos se precisar
}
