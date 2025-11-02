package com.example.feature_fornecedor;

import com.example.feature_fornecedor.dto.ProgramCountResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FeatureCompanyAPI {
    @GET("api/companies/count-workers-by-program/{companyId}")
    Call<List<ProgramCountResponse>> countWorkersByProgram(@Path("companyId") int companyId);
}
