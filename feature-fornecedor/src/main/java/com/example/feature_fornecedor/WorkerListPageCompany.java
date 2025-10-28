package com.example.feature_fornecedor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_fornecedor.ListPage.ListAdapter;
import com.example.feature_fornecedor.ListPage.ListAPI;
import com.example.feature_fornecedor.ListPage.Worker;
import com.example.feature_fornecedor.ui.bottomnav.CompanyBottomNavView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerListPageCompany extends Fragment {

    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    private static final String DL_WORKER = "app.internal://profile/worker/";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_worker_list_page_company, container, false);

        CompanyBottomNavView bottom = v.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,   // troféu (awards)
                    R.id.HomePageCompany,      // home
                    R.id.WorkerListPageCompany // pessoas (team)
            );

            bottom.setActive(CompanyBottomNavView.Item.TEAM, false);
        }

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) RecyclerView da tela
        recyclerView = view.findViewById(R.id.item_worker);

        // 2) Adapter com clique para navegar por URI (sem depender do :core)
        listAdapter = new ListAdapter(new ArrayList<>(), requireContext(), worker -> {
            // worker aqui deve ser um objeto Worker
            String workerId = String.valueOf(worker.getId());

            // Navegação via deeplink interno (NavController.resolve URI)
            Uri uri = Uri.parse(DL_WORKER + workerId);
            Navigation.findNavController(view).navigate(uri);
        });

        // 3) Layout + adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(listAdapter);

        // 4) Buscar dados da API
        listWorkers();
    }

    private void listWorkers() {
        ListAPI listAPI = RetrofitClientPostgres
                .getInstance(requireContext()) // versão que lê URL/token de resources
                .create(ListAPI.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);

        int company_id = prefs.getInt("user_id", -1);

        if (company_id == -1) {
            Toast.makeText(requireContext(), "Erro ao buscar dados da empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        String companyId = String.valueOf(company_id);

        listAPI.getWorkersByCompany(companyId).enqueue(new Callback<List<Worker>>() {
            @Override public void onResponse(@NonNull Call<List<Worker>> call, @NonNull Response<List<Worker>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listAdapter.updateData(response.body());
                } else {
                    Log.e("API_ERROR", "HTTP " + response.code() + " - " + response.message());
                }
            }
            @Override public void onFailure(@NonNull Call<List<Worker>> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", "getWorkersByCompany", t);
            }
        });
    }
}
