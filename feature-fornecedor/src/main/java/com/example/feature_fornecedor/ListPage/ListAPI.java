// Arquivo: com/example/feature_fornecedor/ListPage/ListAPI.java

package com.example.feature_fornecedor.ListPage;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ListAPI {

    @GET("api/workers/list-active-by-companyId/{id}")
    Call<List<Worker>> getWorkersByCompany(@Path("id") String companyId);
}