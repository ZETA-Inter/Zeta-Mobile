package com.example.core;

import static com.example.core.network.RetrofitClientPostgres.ensureSlash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.core.adapter.LessonsCardProgressAdapter;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.request.CompanyPatchRequest;
import com.example.core.dto.request.WorkerPatchRequest;
import com.example.core.dto.response.GoalProgress;
import com.example.core.dto.response.ProgramWorkerResponseDTO;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.network.RetrofitClientPostgres;
import com.example.core.ui.CircularProgressView;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends Fragment implements LessonsCardProgressAdapter.OnLessonClickListener {

    private ApiPostgresClient api;
    private static final String TAG = "ProfileFragment";

    private RecyclerView recyclerCursosAndamento;
    private View loadingAndamentoLayout;
    private LessonsCardProgressAdapter andamentoLessonsAdapter;
    private CircularProgressView circularProgressGoals, circularProgressPrograms;

    // Câmera
    private ImageButton btnCamera;
    private String currentPhotoPath;

    private ImageView photo;

    // Cloudinary (OkHttp singleton)
    private final OkHttpClient http = new OkHttpClient();

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getContext(), "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
                }
            });


    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (currentPhotoPath != null) {
                        File photoFile = new File(currentPhotoPath);
                        // Atualiza imediatamente com a foto local (feedback ao usuário)
                        View v = getView();
                        if (v != null) {
                            View avatar = v.findViewById(R.id.photo);
                            if (avatar instanceof android.widget.ImageView) {
                                ((android.widget.ImageView) avatar).setImageURI(Uri.fromFile(photoFile));
                            }
                        }
                        // Upload + PATCH
                        uploadAndSaveProfilePicture(photoFile);
                    } else {
                        Toast.makeText(getContext(), "Erro ao obter o caminho da foto.", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(getContext(), "Captura de foto cancelada.", Toast.LENGTH_SHORT).show();
                }
                currentPhotoPath = null;
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        api = RetrofitClientPostgres.getApiService(requireContext());

        photo = view.findViewById(R.id.photo);

        recyclerCursosAndamento = view.findViewById(R.id.rv_doing_programs);
        loadingAndamentoLayout = view.findViewById(R.id.layout_cursos_andamento_loading);
        circularProgressGoals = view.findViewById(R.id.circularProgressGoals);
        circularProgressPrograms = view.findViewById(R.id.circularProgressPrograms);
        btnCamera = view.findViewById(R.id.btnCamera);

        andamentoLessonsAdapter = new LessonsCardProgressAdapter(this, getContext());
        recyclerCursosAndamento.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCursosAndamento.setAdapter(andamentoLessonsAdapter);

        ImageView btComeback = view.findViewById(R.id.btComeback);

        btComeback.setOnClickListener(v -> {
            NavController nav = NavHostFragment.findNavController(Profile.this);
            if (!nav.popBackStack()) {
                requireActivity().onBackPressed();
            }
        });


        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int id = sp.getInt("user_id", -1);
        String kind = sp.getString("tipo_usuario", null);
        String name = sp.getString("name", "Usuário");
        String imageUrl = sp.getString("image_url", null);

        Uri data = getActivity().getIntent().getData();
        if (data != null) {
            Log.d(TAG, "Bundle Worker");
            id = Integer.parseInt(data.getQueryParameter("workerId"));
            kind = data.getQueryParameter("tipoUsuario");
            name = data.getQueryParameter("name");
            imageUrl = data.getQueryParameter("imageUrl");
        }

        if (id <= 0 || kind == null) {
            Toast.makeText(getContext(), "Parâmetros inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.perfil)
                    .error(R.drawable.perfil)
                    .into(photo);
        } else {
            photo.setImageResource(R.drawable.perfil);
        }

        ((TextView) view.findViewById(R.id.nome_worker)).setText(name);

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    dispatchTakePictureIntent();
                }
            });
        } else {
            Log.e(TAG, "btnCamera não encontrado no layout. Verifique @+id/btnCamera no XML.");
        }

        if ("COMPANY".equals(kind)) {
            fetchCompanyProgress(id);
            fetchCompanyPrograms(id);
        } else {
            fetchWorkerProgress(id);
            fetchWorkerPrograms(id);
        }
    }

    // ---------------------- CÂMERA ----------------------
    // ---------------------- CÂMERA ----------------------
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Verifica se existe app de câmera
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao criar arquivo de imagem", e);
                Toast.makeText(getContext(), "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile
                );

                // Adiciona permissões temporárias para o app de câmera
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(getContext(), "Nenhum aplicativo de câmera encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Use getExternalFilesDir para que fique privado do app
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    // ---------------------- CLOUDINARY + PATCH ----------------------
    private void uploadAndSaveProfilePicture(File photoFile) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int id = sp.getInt("user_id", -1);
        String kind = sp.getString("tipo_usuario", null);

        if (id <= 0 || kind == null) {
            Toast.makeText(getContext(), "Sessão inválida para atualizar a imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Enviando imagem...", Toast.LENGTH_SHORT).show();

        // Lê config do Cloudinary dos resources
        String cloudName = safeString(getString(R.string.core_cloudinary_cloud_name));
        String uploadPreset = safeString(getString(R.string.core_cloudinary_upload_preset)); // se preenchido → unsigned
        String apiKey = safeString(getString(R.string.core_cloudinary_api_key));
        String apiSecret = safeString(getString(R.string.core_cloudinary_api_secret));

        if (cloudName.isEmpty()) {
            Toast.makeText(getContext(), "Cloudinary: 'cloud_name' não configurado.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Arquivo
        RequestBody fileBody = RequestBody.create(photoFile, MediaType.parse("image/jpeg"));
        mb.addFormDataPart("file", photoFile.getName(), fileBody);

        // Estratégia: unsigned (preferível se preset configurado) ou signed
        boolean useUnsigned = !uploadPreset.isEmpty();

        if (useUnsigned) {
            mb.addFormDataPart("upload_preset", uploadPreset);
            // (opcional) public_id, folder, etc.
        } else {
            // signed: exige api_key + signature (sha1 of params string + api_secret) + timestamp
            if (apiKey.isEmpty() || apiSecret.isEmpty()) {
                Toast.makeText(getContext(), "Cloudinary: configure upload_preset (unsigned) ou api_key/api_secret (signed).", Toast.LENGTH_LONG).show();
                return;
            }
            long timestamp = System.currentTimeMillis() / 1000L;

            // Params a serem assinados (ordem alfabética sem file): ex.: "timestamp=...<api_secret>"
            String toSign = "timestamp=" + timestamp + apiSecret;
            String signature = sha1Hex(toSign);

            mb.addFormDataPart("timestamp", String.valueOf(timestamp));
            mb.addFormDataPart("api_key", apiKey);
            mb.addFormDataPart("signature", signature);
        }

        Request request = new Request.Builder()
                .url(url)
                .post(mb.build())
                .build();

        http.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e(TAG, "Cloudinary upload falhou", e);
                runOnUiThreadSafe(() ->
                        Toast.makeText(getContext(), "Falha ao enviar imagem.", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                okhttp3.ResponseBody rb = response.body();
                String json = (rb != null) ? rb.string() : "";

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Cloudinary HTTP " + response.code() + " - " + json);
                    runOnUiThreadSafe(() ->
                            Toast.makeText(getContext(), "Erro no upload (" + response.code() + ")", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                String secureUrl = parseSecureUrl(json);
                if (secureUrl == null || secureUrl.isEmpty()) {
                    Log.e(TAG, "Cloudinary: secure_url ausente. Resposta: " + json);
                    runOnUiThreadSafe(() ->
                            Toast.makeText(getContext(), "Upload sem URL de retorno.", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // PATCH de acordo com o tipo do usuário
                if ("COMPANY".equals(kind)) {
                    patchCompanyProfile(id, secureUrl);
                } else {
                    patchWorkerProfile(id, secureUrl);
                }
            }
        });
    }

    // PATCH worker
    private void patchWorkerProfile(int workerId, String imageUrl) {
        WorkerPatchRequest req = new WorkerPatchRequest();
        req.setImageUrl(imageUrl);

        api.patchWorker(workerId, req).enqueue(new retrofit2.Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<WorkerResponse> call, @NonNull retrofit2.Response<WorkerResponse> response) {
                if (response.isSuccessful()) {
                    runOnUiThreadSafe(() -> {
                        Toast.makeText(getContext(), "Foto de Worker atualizada!", Toast.LENGTH_SHORT).show();
                        // Se quiser forçar reload do avatar a partir da URL remota:
                         Glide.with(Profile.this)
                                 .load(imageUrl)
                                 .placeholder(R.drawable.perfil)
                                 .error(R.drawable.perfil)
                                 .into(photo);
                    });
                } else {
                    Log.e(TAG, "PATCH worker falhou: " + response.code());
                    runOnUiThreadSafe(() -> Toast.makeText(getContext(), "Erro ao salvar imagem no backend (Worker)", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<WorkerResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "PATCH worker erro", t);
                runOnUiThreadSafe(() -> Toast.makeText(getContext(), "Falha de rede ao salvar imagem (Worker)", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // PATCH company
    private void patchCompanyProfile(int companyId, String imageUrl) {
        CompanyPatchRequest req = new CompanyPatchRequest();
        req.setImageUrl(imageUrl);

        String baseUrl = ensureSlash(requireContext().getString(R.string.core_api_base_url));
        final String tokenPref = requireContext().getString(R.string.core_api_token);

        Log.d("TOKEN_DEBUG", "Token configurado: " + tokenPref);


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder b = original.newBuilder()
                                .header("Accept", "application/json");
                        if (tokenPref != null && !tokenPref.trim().isEmpty()) {
                            b.header("Authorization", "Bearer " + tokenPref.trim());
                        }

                        Request request = b.method(original.method(), original.body()).build();
                        Log.d("Retrofit", "Headers enviados: " + request.headers());
                        return chain.proceed(request);                                }
                })
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        ApiPostgresClient apiPostgresClient = retrofit.create(ApiPostgresClient.class);

        apiPostgresClient.patchCompany(companyId, req).enqueue(new retrofit2.Callback<CompanyResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<CompanyResponse> call, @NonNull retrofit2.Response<CompanyResponse> response) {
                if (response.isSuccessful()) {
                    runOnUiThreadSafe(() -> {
                        Toast.makeText(getContext(), "Logo da empresa atualizada!", Toast.LENGTH_SHORT).show();
                        Glide.with(Profile.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.perfil)
                                .error(R.drawable.perfil)
                                .into(photo);
                    });
                } else {
                    Log.e(TAG, "PATCH company falhou: " + response.code());
                    try {
                        Log.e(TAG, "Corpo de erro: " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    runOnUiThreadSafe(() -> Toast.makeText(getContext(), "Erro ao salvar imagem no backend (Company)", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<CompanyResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "PATCH company erro", t);
                runOnUiThreadSafe(() -> Toast.makeText(getContext(), "Falha de rede ao salvar imagem (Company)", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ---------------------- Helpers Cloudinary ----------------------
    private String safeString(String s) {
        return (s == null) ? "" : s.trim();
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(r);
    }

    // Extrai "secure_url" do JSON (parsing simples para evitar dependência de JSON libs aqui)
    private String parseSecureUrl(@NonNull String json) {
        // Muito simples: procura pelo campo "secure_url":"...".
        // Recomendo usar Gson/JSONObject no seu projeto real.
        String key = "\"secure_url\":\"";
        int i = json.indexOf(key);
        if (i < 0) return null;
        int start = i + key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end).replace("\\/", "/");
    }

    private String sha1Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            Formatter f = new Formatter();
            for (byte value : b) f.format("%02x", value);
            String out = f.toString();
            f.close();
            return out;
        } catch (Exception e) {
            Log.e(TAG, "SHA1 error", e);
            return "";
        }
    }

    // ---------------------- Sua lógica já existente ----------------------
    private void fetchCompanyProgress(int companyId) {
        fetchCompanyGoalProgress(companyId);
        fetchCompanyProgramProgress(companyId);
    }

    private void fetchCompanyProgramProgress(int companyId) {
        api.findAverageProgressPercentageById(companyId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int startColor = Color.parseColor("#043253");
                    int endColor = Color.parseColor("#0F5ECB");
                    circularProgressPrograms.setGradientColors(startColor, endColor);
                    circularProgressPrograms.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de cursos", t);
            }
        });
    }

    private void fetchCompanyGoalProgress(int companyId) {
        api.findPercentageFinishedGoalsById(companyId).enqueue(new Callback<GoalProgress>() {
            @Override
            public void onResponse(@NonNull Call<GoalProgress> call, @NonNull Response<GoalProgress> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int startColor = Color.parseColor("#043253");
                    int endColor = Color.parseColor("#0F5ECB");
                    circularProgressGoals.setGradientColors(startColor, endColor);

                    GoalProgress gp = response.body();

                    int percentage = (int) ((gp.getCompletedGoals() * 100.0) / gp.getTotalGoals());

                    circularProgressGoals.setProgress(percentage);                }
            }

            @Override
            public void onFailure(@NonNull Call<GoalProgress> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de metas", t);
            }
        });
    }

    private void fetchWorkerProgress(int workerId) {
        fetchWorkerGoalProgress(workerId);
        fetchWorkerProgramProgress(workerId);
    }

    private void fetchWorkerGoalProgress(int workerId) {
        api.findOverallGoalsProgressById(workerId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressGoals.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de metas", t);
            }
        });
    }

    private void fetchWorkerProgramProgress(int workerId) {
        api.findOverallProgramsProgressById(workerId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    circularProgressPrograms.setProgress(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao buscar progresso de cursos", t);
            }
        });
    }

    private void fetchCompanyPrograms(int companyId) {
        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        api.listActualProgramsByCompanyId(companyId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    andamentoLessonsAdapter.submitList(new ArrayList<>(inProgress));
                } else {
                    loadingAndamentoLayout.setVisibility(View.VISIBLE);
                    recyclerCursosAndamento.setVisibility(View.GONE);
                    Log.e(TAG, "Falha ao carregar programas. CODE: " + response.code() + " URL: " + call.request().url());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Throwable t) {
                loadingAndamentoLayout.setVisibility(View.VISIBLE);
                recyclerCursosAndamento.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void fetchWorkerPrograms(Integer workerId) {
        recyclerCursosAndamento.setVisibility(View.GONE);
        loadingAndamentoLayout.setVisibility(View.VISIBLE);

        api.listActualProgramsById(workerId).enqueue(new Callback<List<ProgramWorkerResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Response<List<ProgramWorkerResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingAndamentoLayout.setVisibility(View.GONE);
                    recyclerCursosAndamento.setVisibility(View.VISIBLE);
                    List<ProgramWorkerResponseDTO> programs = response.body();

                    List<ProgramWorkerResponseDTO> inProgress = programs.stream()
                            .filter(p -> p.getProgressPercentage() > 0 && p.getProgressPercentage() < 100)
                            .collect(Collectors.toList());

                    andamentoLessonsAdapter.submitList(new ArrayList<>(inProgress));
                } else {
                    loadingAndamentoLayout.setVisibility(View.VISIBLE);
                    recyclerCursosAndamento.setVisibility(View.GONE);
                    Log.e(TAG, "Falha ao carregar programas. CODE: " + response.code() + " URL: " + call.request().url());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProgramWorkerResponseDTO>> call, @NonNull Throwable t) {
                loadingAndamentoLayout.setVisibility(View.VISIBLE);
                recyclerCursosAndamento.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void logHttpError(retrofit2.Response<?> resp) {
        Log.e(TAG, "HTTP " + resp.code() + " - " + resp.message());
    }

    @Override
    public void onLessonClick(ProgramWorkerResponseDTO item) {
        // Lógica de clique de lição
    }
}