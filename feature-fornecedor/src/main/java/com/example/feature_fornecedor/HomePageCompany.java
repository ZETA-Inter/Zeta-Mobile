package com.example.feature_fornecedor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.core.network.RetrofitClientPostgres;
import com.example.feature_fornecedor.dto.ProgramCountResponse;
import com.example.feature_fornecedor.ui.bottomnav.CompanyBottomNavView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomePageCompany extends Fragment {

    private enum AnimalType { AVE, BOVINO, SUINO }
    private AnimalType selectedType = AnimalType.BOVINO;

    private BarChart barChart;
    private FeatureCompanyAPI api;
    private final List<ProgramCountResponse> cachedCounts = new ArrayList<>();

    public HomePageCompany() { }

    public static HomePageCompany newInstance(String param1, String param2) {
        HomePageCompany fragment = new HomePageCompany();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String last = sp.getString("last_animal_filter", "BOVINO");
        try { selectedType = AnimalType.valueOf(last); } catch (Exception ignored) {}
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_page_company, container, false);

        CompanyBottomNavView bottom = v.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,
                    R.id.HomePageCompany,
                    R.id.WorkerListPageCompany
            );
            bottom.setActive(CompanyBottomNavView.Item.HOME, false);
        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgProfile = view.findViewById(R.id.imgProfile);
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

        MaterialButton btnAve = view.findViewById(R.id.btnAve);
        MaterialButton btnBovino = view.findViewById(R.id.btnBovino);
        MaterialButton btnSuino = view.findViewById(R.id.btnSuino);

        applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);

        btnAve.setOnClickListener(v -> {
            if (selectedType != AnimalType.AVE) {
                selectedType = AnimalType.AVE;
                applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);
                persistFilter(selectedType);
                drawFromCache(selectedType);
            }
        });

        btnBovino.setOnClickListener(v -> {
            if (selectedType != AnimalType.BOVINO) {
                selectedType = AnimalType.BOVINO;
                applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);
                persistFilter(selectedType);
                drawFromCache(selectedType);
            }
        });

        btnSuino.setOnClickListener(v -> {
            if (selectedType != AnimalType.SUINO) {
                selectedType = AnimalType.SUINO;
                applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);
                persistFilter(selectedType);
                drawFromCache(selectedType);
            }
        });

        barChart = view.findViewById(R.id.barChart);
        setupBarChart();

        Retrofit retrofit = RetrofitClientPostgres.getInstance(requireContext());
        api = retrofit.create(FeatureCompanyAPI.class);

        int companyId = sp.getInt("user_id", -1);
        fetchCountsAndDraw(companyId, selectedType);

        CompanyBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,
                    R.id.HomePageCompany,
                    R.id.WorkerListPageCompany
            );
            bottom.setActive(CompanyBottomNavView.Item.HOME, false);
        }
    }

    private void persistFilter(AnimalType type) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        sp.edit().putString("last_animal_filter", type.name()).apply();
    }

    private void applyFilterUI(MaterialButton btnAve,
                               MaterialButton btnBovino,
                               MaterialButton btnSuino,
                               AnimalType selected) {

        int cPrimary = ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark);
        int cWhite   = ContextCompat.getColor(requireContext(), com.example.core.R.color.white);
        int cBgLight = ContextCompat.getColor(requireContext(), com.example.core.R.color.bg_light);
        int cText    = ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark);

        styleUnselected(btnAve, cBgLight, cText);
        styleUnselected(btnBovino, cBgLight, cText);
        styleUnselected(btnSuino, cBgLight, cText);

        switch (selected) {
            case AVE:
                styleSelected(btnAve, cPrimary, cWhite);
                break;
            case BOVINO:
                styleSelected(btnBovino, cPrimary, cWhite);
                break;
            case SUINO:
                styleSelected(btnSuino, cPrimary, cWhite);
                break;
        }
    }

    private void styleSelected(MaterialButton b, int bg, int text) {
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bg));
        b.setTextColor(text);
        b.setStrokeWidth(0);
    }

    private void styleUnselected(MaterialButton b, int bg, int text) {
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bg));
        b.setTextColor(text);
        b.setStrokeWidth(1);
        b.setStrokeColor(android.content.res.ColorStateList.valueOf(bg));
    }

    private void setupBarChart() {
        if (barChart == null) return;

        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setScaleXEnabled(true);
        barChart.setScaleYEnabled(true);
        barChart.setDoubleTapToZoomEnabled(true);

        Description d = new Description();
        d.setText("");
        barChart.setDescription(d);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(true);

        barChart.setNoDataText("Sem dados para exibir.");
    }

    private void updateBarChart(List<ChartPoint> points) {
        if (barChart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            ChartPoint p = points.get(i);
            entries.add(new BarEntry(i, p.value));
            labels.add(p.label);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        barChart.invalidate();
        barChart.animateY(500);
    }

    private void fetchCountsAndDraw(int companyId, AnimalType initialSelection) {
        if (companyId <= 0) {
            Toast.makeText(requireContext(), "Empresa inválida para gráfico.", Toast.LENGTH_SHORT).show();
            updateBarChart(new ArrayList<>());
            return;
        }

        api.countWorkersByProgram(companyId).enqueue(new Callback<List<ProgramCountResponse>>() {
            @Override
            public void onResponse(Call<List<ProgramCountResponse>> call, Response<List<ProgramCountResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    cachedCounts.clear();
                    cachedCounts.addAll(response.body());
                    drawFromCache(initialSelection);
                } else {
                    cachedCounts.clear();
                    updateBarChart(new ArrayList<>());
                    Toast.makeText(requireContext(),
                            "Falha ao carregar dados do gráfico (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProgramCountResponse>> call, Throwable t) {
                if (!isAdded()) return;
                cachedCounts.clear();
                updateBarChart(new ArrayList<>());
                Toast.makeText(requireContext(),
                        "Erro de rede ao carregar gráfico: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawFromCache(AnimalType type) {
        String target = type.name(); // "AVE", "BOVINO", "SUINO"
        List<ChartPoint> points = new ArrayList<>();

        for (ProgramCountResponse p : cachedCounts) {
            String seg = normalize(p.getSegment());
            if (target.equals(seg)) {
                String label = (p.getProgramName() != null) ? p.getProgramName() : ("Prog " + p.getProgramId());
                float value = p.getCountWorkers();
                points.add(new ChartPoint(label, value));
            }
        }
        updateBarChart(points);
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toUpperCase(Locale.ROOT);
        s = s
                .replace("Á","A").replace("Ã","A").replace("Â","A").replace("À","A")
                .replace("É","E").replace("Ê","E")
                .replace("Í","I")
                .replace("Ó","O").replace("Õ","O").replace("Ô","O")
                .replace("Ú","U")
                .replace("Ç","C");
        if (s.contains("BOV")) return "BOVINO";
        if (s.contains("AV"))  return "AVE";
        if (s.contains("SUIN") || s.contains("SUINO")) return "SUINO";
        if ("AVE".equals(s) || "BOVINO".equals(s) || "SUINO".equals(s)) return s;
        return s;
    }
}
