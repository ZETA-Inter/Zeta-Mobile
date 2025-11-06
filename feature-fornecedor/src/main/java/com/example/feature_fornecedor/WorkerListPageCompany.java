package com.example.feature_fornecedor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkRequest;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.core.Profile;
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
    private ImageView iconNotificacao;

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

        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        iconNotificacao= view.findViewById(R.id.btnBell);

        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String imageUrl = sp.getString("image_url", null);


        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(com.example.core.R.drawable.perfil)
                    .error(com.example.core.R.drawable.perfil)
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(com.example.core.R.drawable.perfil);
        }

        imgProfile.setOnClickListener(v -> {
            int companyId = sp.getInt("user_id", -1);
            if (companyId <= 0) {
                Toast.makeText(requireContext(), "Perfil indisponível: ID do usuário não encontrado na sessão.", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri deeplink = Uri.parse("app://Core/Profile");

            NavController nav = NavHostFragment.findNavController(this);
            nav.navigate(deeplink);
        });

        // Botão de Settings (engrenagem do header)
        ImageView btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(v);
                try {
                    // Caminho principal por ID do destino
                    nav.navigate(R.id.SettingsFragment);
                } catch (Exception e) {
                    // Fallback por deep link (requer <deepLink app:uri="app://Fornecedor/Settings" />)
                    nav.navigate(Uri.parse("app://Fornecedor/Settings"));
                }
            });
        }


        Uri deeplink = Uri.parse("app://Worker/CardNotificacao");

        iconNotificacao.setOnClickListener(v->{
            Navigation.findNavController(v).navigate(deeplink);
        });

        // 1) RecyclerView da tela
        recyclerView = view.findViewById(R.id.item_worker);

        // 2) Adapter com clique para navegar por URI (sem depender do :core)
        listAdapter = new ListAdapter(new ArrayList<>(), requireContext(), worker -> {
            String workerId = String.valueOf(worker.getId());
            String tipoUsuario = "WORKER";
            String name = worker.getName();
            String imageUrl2 = worker.getImageUrl();

            Uri deeplinkUri = Uri.parse("app://Core/Profile" +
                    "?workerId=" + workerId +
                    "&tipoUsuario=" + tipoUsuario +
                    "&name=" + Uri.encode(name) +
                    "&imageUrl=" + Uri.encode(imageUrl2));

            Navigation.findNavController(view).navigate(deeplinkUri);

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
