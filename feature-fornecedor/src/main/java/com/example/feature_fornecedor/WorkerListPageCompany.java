// Arquivo principal corrigido: WorkerListPageCompany.java
package com.example.feature_fornecedor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.feature_fornecedor.ListPage.ListAdapter;
import com.example.feature_fornecedor.ListPage.ListAPI;
import com.example.feature_fornecedor.ListPage.Worker;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_worker_list_page_company, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.item_worker);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        listWorkers();
    }

    private void listWorkers() {
        String BASE_URL = "https://api-postgresql-zeta-fide.onrender.com/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ListAPI listAPI = retrofit.create(ListAPI.class);

        Call<List<Worker>> call = listAPI.getAllWorker();

        call.enqueue(new Callback<List<Worker>>() {
            @Override
            public void onResponse(@NonNull Call<List<Worker>> call, @NonNull Response<List<Worker>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Worker> workers = response.body();

                    listAdapter = new ListAdapter(workers, requireContext(), worker -> {
                        Navigation.findNavController(requireView()).navigate(R.id.RankingPageCompany);
                    });
                    recyclerView.setAdapter(listAdapter);

                } else {
                    // Log para depuração caso a resposta não seja bem-sucedida
                    Log.e("API_ERROR", "Resposta não foi bem-sucedida. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Worker>> call, @NonNull Throwable t) {
                // CORREÇÃO: É fundamental tratar falhas para depuração.
                Log.e("API_FAILURE", "Falha na chamada à API: ", t);
            }
        });
    }
}