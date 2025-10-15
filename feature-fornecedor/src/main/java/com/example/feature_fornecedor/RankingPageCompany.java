package com.example.feature_fornecedor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.feature_fornecedor.ranking.RankingAdapter;
import com.example.feature_fornecedor.ranking.RankingApi;
import com.example.feature_fornecedor.ranking.RankingEntry;
import com.example.feature_fornecedor.ranking.RetrofitClient;
import com.example.feature_fornecedor.ranking.RankingAdapter;
import com.example.feature_fornecedor.ui.bottomnav.CompanyBottomNavView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingPageCompany extends Fragment {

    private ShapeableImageView imgFirst, imgSecond, imgThird;
    private TextView nameFirst, nameSecond, nameThird;
    private TextView ptsFirst, ptsSecond, ptsThird;
    private RankingAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ranking_page_company, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        imgFirst  = v.findViewById(R.id.imgFirst);
        imgSecond = v.findViewById(R.id.imgSecond);
        imgThird  = v.findViewById(R.id.imgThird);

        nameFirst  = v.findViewById(R.id.nameFirst);
        nameSecond = v.findViewById(R.id.nameSecond);
        nameThird  = v.findViewById(R.id.nameThird);

        ptsFirst  = v.findViewById(R.id.ptsFirst);
        ptsSecond = v.findViewById(R.id.ptsSecond);
        ptsThird  = v.findViewById(R.id.ptsThird);

        RecyclerView rv = v.findViewById(R.id.rvRanking);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);
        rv.setItemAnimator(null);
        adapter = new RankingAdapter();
        rv.setAdapter(adapter);

        CompanyBottomNavView bottom = v.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,
                    R.id.HomePageCompany,
                    R.id.WorkerListPageCompany
            );
            bottom.setActive(CompanyBottomNavView.Item.AWARDS, false);
        }

        fetchRanking();
    }


    private void fetchRanking() {
        RankingApi api = RetrofitClient.get().create(RankingApi.class);

        final String FULL_URL = "https://api-postgresql-zeta-fide.onrender.com/api/companies/ranking?companyId=1";

        api.getCompanyRanking(FULL_URL).enqueue(new retrofit2.Callback<com.google.gson.JsonElement>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonElement> call,
                                   @NonNull retrofit2.Response<com.google.gson.JsonElement> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    // erro HTTP ou corpo vazio
                    bindPodium(null, null, null);
                    adapter.submit(new java.util.ArrayList<>(), 4);
                    return;
                }

                java.util.List<RankingEntry> all = mapJsonToEntries(resp.body());

                // ordena: se houver 'position', usa; senão, por pontos desc
                java.util.Collections.sort(all, (a, b) -> {
                    if (a.position != null && b.position != null) return Integer.compare(a.position, b.position);
                    if (a.position != null) return -1;
                    if (b.position != null) return 1;
                    int pa = a.points != null ? a.points : 0;
                    int pb = b.points != null ? b.points : 0;
                    return Integer.compare(pb, pa);
                });

                RankingEntry first  = findByPosition(all, 1);
                RankingEntry second = findByPosition(all, 2);
                RankingEntry third  = findByPosition(all, 3);
                if (first == null && all.size() > 0)  first  = all.get(0);
                if (second == null && all.size() > 1) second = all.get(1);
                if (third == null && all.size() > 2)  third  = all.get(2);

                bindPodium(first, second, third);

                java.util.List<RankingEntry> rest = new java.util.ArrayList<>();
                for (RankingEntry e : all) {
                    boolean isTop3 = (e == first) || (e == second) || (e == third)
                            || (e.position != null && (e.position == 1 || e.position == 2 || e.position == 3));
                    if (!isTop3) rest.add(e);
                }
                if (rest.isEmpty() && all.size() > 3) {
                    rest = new java.util.ArrayList<>(all.subList(3, all.size()));
                }

                adapter.submit(rest, 4);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonElement> call, @NonNull Throwable t) {
                bindPodium(null, null, null);
                adapter.submit(new java.util.ArrayList<>(), 4);
            }
        });
    }

    /** Encontra item por posição (1,2,3...) */
    @Nullable
    private RankingEntry findByPosition(java.util.List<RankingEntry> list, int pos) {
        for (RankingEntry e : list) {
            if (e.position != null && e.position == pos) return e;
        }
        return null;
    }

    /** Converte qualquer JSON (array direto ou objeto com "data", etc.) em lista de RankingEntry */
    private java.util.List<RankingEntry> mapJsonToEntries(com.google.gson.JsonElement root) {
        java.util.List<RankingEntry> out = new java.util.ArrayList<>();

        com.google.gson.JsonArray arr = null;

        if (root.isJsonArray()) {
            arr = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            com.google.gson.JsonObject obj = root.getAsJsonObject();
            // tenta achar um array dentro (data, result, items, content, records...)
            String[] keys = {"data","result","items","content","records","ranking","users"};
            for (String k : keys) {
                if (obj.has(k) && obj.get(k).isJsonArray()) {
                    arr = obj.getAsJsonArray(k);
                    break;
                }
            }
            // fallback: se o objeto for um único item, trata como array de 1
            if (arr == null) {
                arr = new com.google.gson.JsonArray();
                arr.add(obj);
            }
        }

        if (arr == null) return out;

        for (com.google.gson.JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            com.google.gson.JsonObject o = el.getAsJsonObject();

            RankingEntry e = new RankingEntry();
            e.id       = getInt(o,  "id","workerId","codigo","idWorker");
            e.name     = getStr(o,  "name","workerName","nome","worker","fullName");
            e.position = getInt(o,  "position","rankPosition","posicao","rank","rankingPosition","ordem","order","place");
            e.points   = getInt(o,  "points","pontuacao","score","pts","totalPoints");
            e.photo    = getStr(o,  "photo","avatar","foto","imageUrl","photoUrl","urlFoto","image");

            out.add(e);
        }
        return out;
    }

    @Nullable
    private Integer getInt(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) if (o.has(k) && !o.get(k).isJsonNull()) {
            try { return o.get(k).getAsInt(); } catch (Exception ignore) {}
            try { return Integer.parseInt(o.get(k).getAsString()); } catch (Exception ignore) {}
        }
        return null;
    }

    @Nullable
    private String getStr(com.google.gson.JsonObject o, String... keys) {
        for (String k : keys) if (o.has(k) && !o.get(k).isJsonNull()) {
            try { return o.get(k).getAsString(); } catch (Exception ignore) {}
        }
        return null;
    }


    private void bindPodium(@Nullable RankingEntry first,
                            @Nullable RankingEntry second,
                            @Nullable RankingEntry third) {
        setSlot(imgFirst,  nameFirst,  ptsFirst,  first);
        setSlot(imgSecond, nameSecond, ptsSecond, second);
        setSlot(imgThird,  nameThird,  ptsThird,  third);
    }

    private void setSlot(ShapeableImageView img, TextView name, TextView pts, @Nullable RankingEntry e) {
        if (e == null) {
            name.setText("—");
            pts.setText("0 pts");
            img.setImageResource(R.drawable.perfil);
            return;
        }
        name.setText(e.name != null ? e.name : "—");
        int p = e.points != null ? e.points : 0;
        pts.setText(p + " pts");

        Glide.with(img.getContext())
                .load(e.photo)
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(img);
    }
}
