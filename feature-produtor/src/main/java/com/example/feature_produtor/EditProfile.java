package com.example.feature_produtor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class EditProfile extends Fragment {

    // UI
    private ImageView profileImage;
    private EditText editNome;
    private EditText editDescricao;
    private EditText editAniversario;
    private Button btnSalvar;

    // Cloudinary e Upload
    private final String CLOUD_NAME = "dkhehxb76";
    private final String UPLOAD_PRESET = "zetaProject";
    private String currentImageUrl = null; // URL da imagem atual após o upload

    // Câmera e Permissões
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri photoUri;

    // Configuração da API (Se for salvar a URL da imagem aqui, descomente)
    // private ApiPostgres apiPostgres;

    public EditProfile() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLaunchers();

        // Se precisar inicializar Retrofit aqui para salvar a imagem
        // initRetrofit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false); // *ASSUMIR: fragment_edit_profile.xml*

        // 1. Inicializa
        profileImage = view.findViewById(R.id.edit_profile_image);
        editNome = view.findViewById(R.id.edit_text_name);
        editDescricao = view.findViewById(R.id.edit_text_descricao);
        editAniversario = view.findViewById(R.id.edit_text_niver);
        btnSalvar = view.findViewById(R.id.btn_salvar);

        // 2. Tenta inicializar o Cloudinary
        initCloudinary();

        // 3. Configura Listeners
        setupClickListeners(view);

        // 4. Carrega dados existentes (Se houver)
        loadExistingData();

        return view;
    }

    private void initCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", CLOUD_NAME);
        try {
            MediaManager.init(requireContext(), config);
        } catch (IllegalStateException e) {
            // Ignorar se já inicializado
        }
    }



    private void loadExistingData() {
       //gostaria que fossem os dados atuais da tela de perfil
    }

    private void setupClickListeners(View fragmentView) {
        // 1. Mudar Foto de Perfil
        profileImage.setOnClickListener(v -> dispatchTakePictureIntent());

        // 2. Salvar Informações
        btnSalvar.setOnClickListener(v -> {

            // 2.1. Coleta os dados dos campos
            String nome = editNome.getText().toString();
            String descricao = editDescricao.getText().toString();
            String aniversario = editAniversario.getText().toString(); // Idealmente, valide o formato dd/mm/yyyy

            // 2.2. Cria o Bundle para enviar ao ProfileWorker
            Bundle bundle = new Bundle();
            bundle.putString("nome", nome);
            bundle.putString("descricao", descricao);
            bundle.putString("aniversario", aniversario);

            // 2.3. Adiciona a nova URL da imagem, se houver upload
            if (currentImageUrl != null) {
                bundle.putString("imageUrl", currentImageUrl);
            }

            // 2.4. Navega de volta para o Profileworker
            Navigation.findNavController(fragmentView).navigate(com.example.core.R.id.Profile, bundle);

            // Nota: A lógica de salvar os dados no banco (além da imagem) deve ocorrer aqui antes da navegação,
            // ou ser tratada no Fragment de destino (Profileworker).
        });
    }

    // --- Métodos de Câmera e Permissão (Reaproveitados do Profileworker) ---

    private void setupLaunchers() {
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    // Trate o resultado das permissões, se necessário
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && photoUri != null) {
                        uploadImagem(photoUri);
                    } else {
                        Toast.makeText(getContext(), "Foto não tirada ou cancelada.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void dispatchTakePictureIntent() {
        // Certifique-se de que o contexto não é nulo antes de verificar a permissão
        if (getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                openCamera();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Erro ao criar arquivo de foto.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            // Se as permissões não foram concedidas, solicite-as
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(new String[]{ Manifest.permission.CAMERA });
        } else {
            requestPermissionsLauncher.launch(new String[]{ Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE });
        }
    }

    private void openCamera() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        photoUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getApplicationContext().getPackageName() + ".provider",
                photoFile);

        cameraLauncher.launch(photoUri);
    }

    // --- Métodos de Cloudinary e Upload ---

    private void uploadImagem(Uri imagemUri) {
        Toast.makeText(getContext(), "Iniciando upload...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imagemUri)
                .option("folder", "fotosPerfilWorker")
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { /* ... */ }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { /* ... */ }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");

                        // 1. Atualiza a URL para ser enviada no Bundle
                        currentImageUrl = url;

                        // 2. Carrega a nova imagem no ImageView de edição
                        Glide.with(requireContext())
                                .load(url)
                                .into(profileImage);


                        // saveUserImageUrlToDatabase(url);

                        Toast.makeText(getContext(), "Foto atualizada. Clique em SALVAR.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(getContext(), "Erro no upload: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        currentImageUrl = null; // Limpa a URL se o upload falhar
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch(requireContext());
    }

    //    private void saveUserImageUrlToDatabase(String imageUrl) {
//        // 1. Obtenha o ID do usuário (Exemplo: 1, substitua pela sua lógica real)
//        int userId = 1;
//
//        // 2. Crie o corpo da requisição
//        Image requestBody = new Image(imageUrl);
//
//        // 3. Crie a chamada Retrofit
//        Call<User> call = apiPostgres.updateUserImage(userId, requestBody);
//
//        call.enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if (response.isSuccessful()) {
//                    // Atualização no banco de dados bem-sucedida!
//                    Toast.makeText(getContext(), "URL salva no DB com sucesso!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Erro ao salvar URL no DB: " + response.code(), Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                Toast.makeText(getContext(), "Falha de conexão ao salvar URL: " + t.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
}
//}
