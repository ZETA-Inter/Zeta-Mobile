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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.feature_fornecedor.dto.FinishedGoalsPercentage;
import com.example.feature_fornecedor.dto.ProgramCountResponse;
import com.example.feature_fornecedor.dto.ProgramGoalResponse;
import com.example.feature_fornecedor.ui.bottomnav.CompanyBottomNavView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePageCompany extends Fragment {

    private static final String TAG = "HomePageCompany";

    // Enum do filtro
    private enum AnimalType { AVE, BOVINO, SUINO }

    // Estado
    private AnimalType selectedType = AnimalType.BOVINO; // default
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener spListener;

    // API + cache
    private FeatureCompanyAPI api;
    private final List<ProgramCountResponse> cachedCounts = new ArrayList<>();
    private final List<ProgramGoalResponse>  cachedGoals  = new ArrayList<>();

    // === KPIs (IDs diretos do XML)
    private TextView tvKpiCoursesValue; // Conclusão Média - Cursos (%)
    private TextView tvKpiPointsValue;  // Pontuação média
    private TextView tvKpiGoalsValue;   // Porcentagem de Conclusão - Metas (%)

    public HomePageCompany() {}

    public static HomePageCompany newInstance(String p1, String p2) {
        HomePageCompany f = new HomePageCompany();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page_company, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        api   = FeatureCompanyAPI.getInstance(requireContext());

        // ========= Cabeçalho: foto perfil =========
        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        String imageUrl = prefs.getString("image_url", null);
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
            int companyId = prefs.getInt("user_id", -1);
            if (companyId <= 0) {
                Toast.makeText(requireContext(),
                        "Perfil indisponível: ID do usuário não encontrado na sessão.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Uri deeplink = Uri.parse("app://Core/Profile");
            NavController nav = NavHostFragment.findNavController(this);
            nav.navigate(deeplink);
        });

        // ========= Filtros =========
        MaterialButton btnAve    = view.findViewById(R.id.btnAve);
        MaterialButton btnBovino = view.findViewById(R.id.btnBovino);
        MaterialButton btnSuino  = view.findViewById(R.id.btnSuino);

        applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);

        btnAve.setOnClickListener(v -> updateFilter(AnimalType.AVE, btnAve, btnBovino, btnSuino));
        btnBovino.setOnClickListener(v -> updateFilter(AnimalType.BOVINO, btnAve, btnBovino, btnSuino));
        btnSuino.setOnClickListener(v -> updateFilter(AnimalType.SUINO, btnAve, btnBovino, btnSuino));

        // ========= Bottom nav =========
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

        // ========= KPIs: bind dos valores =========
        tvKpiCoursesValue = view.findViewById(R.id.tvKpiCoursesValue);
        tvKpiPointsValue  = view.findViewById(R.id.tvKpiPointsValue);
        tvKpiGoalsValue   = view.findViewById(R.id.tvKpiGoalsValue);

        // Valores default antes do load
        setKpiText(tvKpiCoursesValue, "--%");
        setKpiText(tvKpiPointsValue,  "--");
        setKpiText(tvKpiGoalsValue,   "--%");

        // ========= Primeira carga (aguarda user_id se necessário) =========
        int idNow = prefs.getInt("user_id", -1);
        Log.d(TAG, "companyId (user_id) lido da sessão: " + idNow + " | keys=" + prefs.getAll().keySet());

        if (idNow > 0) {
            // Gráficos
            fetchCountsAndDraw(idNow, selectedType);
            fetchGoalsAndDraw(idNow, selectedType);
            // KPIs
            fetchAllKpis(idNow);
        } else {
            Log.w(TAG, "Esperando o login salvar o user_id...");
            spListener = (sp, key) -> {
                if ("user_id".equals(key)) {
                    int id = sp.getInt("user_id", -1);
                    Log.d(TAG, "Listener detectou user_id = " + id);
                    if (id > 0 && isAdded()) {
                        // Gráficos
                        fetchCountsAndDraw(id, selectedType);
                        fetchGoalsAndDraw(id, selectedType);
                        // KPIs
                        fetchAllKpis(id);
                        prefs.unregisterOnSharedPreferenceChangeListener(spListener);
                    }
                }
            };
            prefs.registerOnSharedPreferenceChangeListener(spListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (prefs != null && spListener != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(spListener);
        }
    }

    // ========================================================
    // Filtros – comportamento e estilos
    // ========================================================
    private void updateFilter(AnimalType newType,
                              MaterialButton btnAve,
                              MaterialButton btnBovino,
                              MaterialButton btnSuino) {
        if (selectedType != newType) {
            selectedType = newType;
            applyFilterUI(btnAve, btnBovino, btnSuino, selectedType);
            dispatchFilter(selectedType);

            int companyId = prefs.getInt("user_id", -1);
            if (companyId > 0) {
                // Recarrega gráficos (KPIs são por companyId apenas; não dependem do segmento)
                fetchCountsAndDraw(companyId, selectedType);
                fetchGoalsAndDraw(companyId, selectedType);
            } else {
                Log.w(TAG, "Sem companyId válido ao trocar filtro.");
            }
        }
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

    private void dispatchFilter(AnimalType type) {
        Bundle result = new Bundle();
        result.putString("type", type.name()); // "AVE","BOVINO","SUINO"
        getParentFragmentManager().setFragmentResult("animal_filter", result);
    }

    // ========================================================
    // GRÁFICO DE BARRAS
    // ========================================================
    private void fetchCountsAndDraw(int companyId, AnimalType initialSelection) {
        if (companyId <= 0) {
            Toast.makeText(requireContext(), "Empresa inválida para gráfico.", Toast.LENGTH_SHORT).show();
            updateBarChart(new ArrayList<>());
            return;
        }

        api.countWorkersByProgram(companyId).enqueue(new Callback<List<ProgramCountResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramCountResponse>> call,
                                   @NonNull Response<List<ProgramCountResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    cachedCounts.clear();
                    cachedCounts.addAll(response.body());
                    drawBarsFromCache(initialSelection);
                } else {
                    cachedCounts.clear();
                    updateBarChart(new ArrayList<>());
                    Toast.makeText(requireContext(),
                            "Falha ao carregar dados do gráfico (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProgramCountResponse>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                cachedCounts.clear();
                updateBarChart(new ArrayList<>());
                Toast.makeText(requireContext(),
                        "Erro de rede ao carregar gráfico: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawBarsFromCache(AnimalType filter) {
        List<ChartPoint> points = new ArrayList<>();
        for (ProgramCountResponse item : cachedCounts) {
            final String seg = (item.getSegment() == null) ? "" : item.getSegment().trim().toLowerCase();
            final String wanted = normalizeSegment(filter);
            if (seg.equals(wanted)) {
                points.add(new ChartPoint(item.getProgramName(), item.getCountWorkers()));
            }
        }
        updateBarChart(points);
    }

    private void updateBarChart(List<ChartPoint> points) {
        BarChart chart = getView().findViewById(R.id.barChart);
        if (chart == null) {
            Log.e(TAG, "BarChart não encontrado no layout!");
            return;
        }

        // Sem dados -> mensagem
        if (points == null || points.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Não temos valor correspondente para esse filtro");
            chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.label_grey));
            Description empty = new Description();
            empty.setText("");
            chart.setDescription(empty);
            chart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            entries.add(new BarEntry(i, points.get(i).getValue()));
            labels.add(points.get(i).getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Funcionários por Programa");
        dataSet.setColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.black));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextSize(9f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(9f);
        chart.getAxisRight().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);

        chart.setData(barData);
        chart.setExtraLeftOffset(-1000f);
        chart.setExtraRightOffset(-12f);
        chart.setMinOffset(12f);

        chart.invalidate();
    }

    // ========================================================
    // GRÁFICO DE LINHA
    // ========================================================
    private void fetchGoalsAndDraw(int companyId, AnimalType filter) {
        if (companyId <= 0) {
            Toast.makeText(requireContext(), "Empresa inválida para gráfico de metas.", Toast.LENGTH_SHORT).show();
            updateLineChart(new ArrayList<>());
            return;
        }

        api.countGoalsByProgram(companyId).enqueue(new Callback<List<ProgramGoalResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramGoalResponse>> call,
                                   @NonNull Response<List<ProgramGoalResponse>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    cachedGoals.clear();
                    cachedGoals.addAll(response.body());
                    drawLineFromCache(filter);
                } else {
                    cachedGoals.clear();
                    updateLineChart(new ArrayList<>());
                    Toast.makeText(requireContext(),
                            "Falha ao carregar dados das metas (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProgramGoalResponse>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                cachedGoals.clear();
                updateLineChart(new ArrayList<>());
                Toast.makeText(requireContext(),
                        "Erro de rede ao carregar gráfico de metas: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawLineFromCache(AnimalType filter) {
        List<ChartPoint> points = new ArrayList<>();
        for (ProgramGoalResponse item : cachedGoals) {
            final String seg = (item.getSegment() == null) ? "" : item.getSegment().trim().toLowerCase();
            final String wanted = normalizeSegment(filter);
            if (seg.equals(wanted)) {
                points.add(new ChartPoint(item.getProgramName(), item.getCountGoals()));
            }
        }
        updateLineChart(points);
    }

    private void updateLineChart(List<ChartPoint> points) {
        LineChart chart = getView().findViewById(R.id.lineChartGoals);
        if (chart == null) {
            Log.e(TAG, "LineChart não encontrado no layout!");
            return;
        }

        // Sem dados -> mensagem
        if (points == null || points.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Não temos valor correspondente para esse filtro");
            chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.label_grey));
            Description empty = new Description();
            empty.setText("");
            chart.setDescription(empty);
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            entries.add(new Entry(i, points.get(i).getValue()));
            labels.add(points.get(i).getLabel());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Metas por Programa");
        dataSet.setColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_dark));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3.5f);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(true);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), com.example.core.R.color.primary_light));

        LineData lineData = new LineData(dataSet);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextSize(9f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(9f);
        chart.getAxisRight().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getLegend().setEnabled(false);

        chart.setData(lineData);
        chart.invalidate();
    }

    private String normalizeSegment(AnimalType type) {
        // Backend: "Bovino", "Aves", "Suíno"
        switch (type) {
            case BOVINO: return "bovino";
            case AVE:    return "aves";
            case SUINO:  return "suíno";
            default:     return "";
        }
    }

    // ========================================================
    // KPIs — chamadas por companyId (não dependem do filtro)
    // ========================================================

    private void fetchAllKpis(int companyId) {
        fetchKpiCoursesAvgCompletion(companyId); // 1) Conclusão Média - Cursos
        fetchKpiAveragePoints(companyId);        // 2) Pontuação média
        fetchKpiGoalsFinishedPercentage(companyId); // 3) % Conclusão - Metas
    }

    // 1) api/companies/average-progress-percentage/{companyId}
    // -> único campo (número). Exibimos como percentual com 1 casa: "62.3%"
    private void fetchKpiCoursesAvgCompletion(int companyId) {
        api.averageProgressPercentage(companyId).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(@NonNull Call<Double> call, @NonNull Response<Double> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Double v = response.body();
                    String formatted = (v == null) ? "--%" : formatPercent(v);
                    setKpiText(tvKpiCoursesValue, formatted);
                } else {
                    Log.w(TAG, "KPI Cursos (% média) HTTP " + response.code());
                    setKpiText(tvKpiCoursesValue, "--%");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Double> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "KPI Cursos (% média) falhou: " + t.getMessage());
                setKpiText(tvKpiCoursesValue, "--%");
            }
        });
    }

    // 2) api/companies/average-points/{companyId}
    // -> único campo (número). Exibimos com 1 casa decimal.
    private void fetchKpiAveragePoints(int companyId) {
        api.averagePoints(companyId).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(@NonNull Call<Double> call, @NonNull Response<Double> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Double v = response.body();
                    String formatted = (v == null) ? "--" : formatOneDecimal(v);
                    setKpiText(tvKpiPointsValue, formatted);
                } else {
                    Log.w(TAG, "KPI Pontuação média HTTP " + response.code());
                    setKpiText(tvKpiPointsValue, "--");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Double> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "KPI Pontuação média falhou: " + t.getMessage());
                setKpiText(tvKpiPointsValue, "--");
            }
        });
    }

    // 3) api/goals/finished-goals-percentage/{companyId}
    // -> Campo usado: totalGoals (percentual). Exibimos como "NN.N%"
    private void fetchKpiGoalsFinishedPercentage(int companyId) {
        api.finishedGoalsPercentage(companyId).enqueue(new Callback<FinishedGoalsPercentage>() {
            @Override
            public void onResponse(@NonNull Call<FinishedGoalsPercentage> call,
                                   @NonNull Response<FinishedGoalsPercentage> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    FinishedGoalsPercentage body = response.body();
                    Double total = (body != null) ? body.getTotalGoals() : null;
                    String formatted = (total == null) ? "--%" : formatPercent(total);
                    setKpiText(tvKpiGoalsValue, formatted);
                } else {
                    Log.w(TAG, "KPI % Metas concluídas HTTP " + response.code());
                    setKpiText(tvKpiGoalsValue, "--%");
                }
            }

            @Override
            public void onFailure(@NonNull Call<FinishedGoalsPercentage> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "KPI % Metas concluídas falhou: " + t.getMessage());
                setKpiText(tvKpiGoalsValue, "--%");
            }
        });
    }

    // ==== Helpers de formatação/UX para KPIs ====
    private String formatPercent(double v) {
        // Caso o backend retorne [0..1], ajuste aqui: v*100
        // Se já vier em % (0..100), mantenha.
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(v) + "%";
    }

    private String formatOneDecimal(double v) {
        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(v);
    }

    private void setKpiText(TextView tv, String value) {
        if (tv != null) tv.setText(value);
    }

}
