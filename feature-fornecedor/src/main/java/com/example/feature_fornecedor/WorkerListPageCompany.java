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
import java.util.Map;

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

    @Nullable
    private String resolveCompanyId(@NonNull Context c) {
        SharedPreferences p = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        Map<String, ?> all = p.getAll();
        Log.d("PREFS_DEBUG", "user_session keys=" + all.keySet());

        // Tenta company_id (String ou Int)
        if (p.contains("company_id")) {
            Object v = all.get("company_id");
            if (v != null) return String.valueOf(v).trim();
        }
        // Variações comuns
        if (p.contains("companyId")) {
            Object v = all.get("companyId");
            if (v != null) return String.valueOf(v).trim();
        }
        // Alguns projetos usam user_id como company
        if (p.contains("user_id")) {
            Object v = all.get("user_id");
            if (v != null && !String.valueOf(v).equals("-1")) return String.valueOf(v).trim();
        }
        return null;
    }


    private void listWorkers() {
        ListAPI listAPI = com.example.core.network.RetrofitClientPostgres
                .getInstance(requireContext())
                .create(ListAPI.class);

        String companyId = resolveCompanyId(requireContext());

        if (companyId == null || companyId.isEmpty()) {
            Toast.makeText(requireContext(), "Erro ao buscar dados da empresa (company_id ausente).", Toast.LENGTH_SHORT).show();
            return;
        }

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
