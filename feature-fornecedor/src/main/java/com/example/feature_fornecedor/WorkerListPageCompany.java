// Arquivo: com/example/feature_fornecedor/WorkerListPageCompany.java

package com.example.feature_fornecedor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.feature_fornecedor.ListPage.ListAdapter;
import com.example.feature_fornecedor.ListPage.ListAPI;
import com.example.feature_fornecedor.ListPage.Worker;
import java.util.ArrayList; // Importar ArrayList
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WorkerListPageCompany extends Fragment {

    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_worker_list_page_company, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView workers = view.findViewById(R.id.item_worker);
        workers.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerView.setAdapter(listAdapter);

        // 2. Chamar a API para buscar os dados
        listWorkers();


        workers.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.ProfileWorker));
    }

    private void listWorkers() {
        String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ListAPI listAPI = retrofit.create(ListAPI.class);
        String companyId = null;

        SharedPreferences prefs = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int company_id = prefs.getInt("user_id", -1);
        if (company_id == -1){
            Toast.makeText(requireContext(), "Erro ao buscar dados da empresa", Toast.LENGTH_SHORT).show();
            companyId = null;
        }
        else {
            companyId = String.valueOf(company_id);
        }

        Call<List<Worker>> call = listAPI.getWorkersByCompany(companyId);

        call.enqueue(new Callback<List<Worker>>() {
            @Override
            public void onResponse(@NonNull Call<List<Worker>> call, @NonNull Response<List<Worker>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Worker> workers = response.body();
                    listAdapter.updateData(workers);
                } else {
                    Log.e("API_ERROR", "Resposta não foi bem-sucedida. Código: " + response.code() + ", Mensagem: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Worker>> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "Falha na chamada à API: ", t);
            }
        });
    }
}