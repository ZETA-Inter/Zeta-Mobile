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


public class Profileworker extends Fragment {

    // VARIÁVEIS DE UI
    private ImageView profileImage;
    private ImageView iconConfig;
    private ImageView iconNotificacao;
    private Button editar;
    private TextView nomePerfil;
    private TextView descricaoPerfil;
    private TextView aniversarioPerfil;

    // VARIÁVEIS DA CÂMERA E PERMISSÕES
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri photoUri;

    // VARIÁVEIS CLOUDINARY
    private final String CLOUD_NAME = "dkhehxb76";
    private final String UPLOAD_PRESET = "zetaProject";

    public Profileworker() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLaunchers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profileworker, container, false);

        profileImage = view.findViewById(R.id.edit_profile_image);
        iconConfig = view.findViewById(R.id.icon_settings);
        iconNotificacao = view.findViewById(R.id.icon_notification);
        editar = view.findViewById(R.id.edit);
        nomePerfil=view.findViewById(R.id.user_name);
        descricaoPerfil=view.findViewById(R.id.dscr);
        aniversarioPerfil=view.findViewById(R.id.dataNiver);


// processa os argumentos recebidos
        processArguments(view);

        // Tenta inicializar o Cloudinary (necessário se não for inicializado na Application/Activity)
        initCloudinary();

        setupClickListeners(view);
        checkPermissions();

        return view;
    }


    private void setupClickListeners(View fragmentView) {
        // Abrir Câmera
        profileImage.setOnClickListener(v -> dispatchTakePictureIntent());

        //  Navegar para Configurações
        iconConfig.setOnClickListener(v -> {
           // Navigation.findNavController(fragmentView).navigate(R.id.);
        });

        //  Navegar para Notificações
        iconNotificacao.setOnClickListener(v -> {
            Navigation.findNavController(fragmentView).navigate(R.id.CardNotificacao);
        });

        editar.setOnClickListener(v -> {
            Navigation.findNavController(fragmentView).navigate(R.id.EditProfile);
        });
    }

    private void processArguments(View view) {
        // Tenta obter o Bundle de argumentos da navegação
        Bundle bundle = getArguments();
        if (bundle != null) {

            // 1. Atualiza o Nome
            String nome = bundle.getString("nome");
            if (nome != null) {
                nomePerfil.setText(nome);
            }

            // 2. Atualiza a Descrição
            String descricao = bundle.getString("descricao");
            if (descricao != null) {
                descricaoPerfil.setText(descricao);
            }

            // 3. Atualiza o Aniversário
            String aniversario = bundle.getString("aniversario");
            if (aniversario != null) {
                aniversarioPerfil.setText(aniversario);
            }

            // 4. Atualiza a Imagem (se houver uma nova URL)
            String imageUrl = bundle.getString("imageUrl");
            if (imageUrl != null) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .into(profileImage);


                // saveUserImageUrlToDatabase(imageUrl);
            }

            //remove info antiga
            setArguments(null);
        }
    }

    // --- Métodos de Câmera e Permissão ---


    private void initCloudinary() {
        Map config = new HashMap();
        // Recomendado: Usar string resource ou credenciais seguras para API Secret
        config.put("cloud_name", CLOUD_NAME);

        try {
            MediaManager.init(requireContext(), config);
        } catch (IllegalStateException e) {
            // Cloudinary já inicializado, ignorar
        }

    }

    private void checkPermissions() {
        // Verifica e solicita permissões necessárias
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.CAMERA
            });
        } else {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void setupLaunchers() {
        // Configura o launcher de permissões
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {

                });

        // Configura o launcher da câmera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && photoUri != null) {
                        // Foto tirada com sucesso, faça o upload
                        uploadImagem(photoUri);
                    } else {
                        Toast.makeText(getContext(), "Foto não tirada ou cancelada.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                openCamera();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Erro ao criar arquivo de foto.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Permissão de Câmera não concedida.", Toast.LENGTH_SHORT).show();
            checkPermissions(); // Solicita permissões novamente se necessário
        }
    }

    private void openCamera() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Diretório para salvar a imagem (melhor usar getExternalFilesDir)
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Gera a Uri usando FileProvider
        photoUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getApplicationContext().getPackageName() + ".provider",
                photoFile);

        // Lança a câmera com o Uri para onde salvar a foto
        cameraLauncher.launch(photoUri);
    }

    // --- Métodos de Cloudinary e Upload ---

    private void uploadImagem(Uri imagemUri) {
        MediaManager.get().upload(imagemUri)
                .option("folder", "fotosPerfilWorker") // Pasta no Cloudinary
                .unsigned(UPLOAD_PRESET) // Seu preset de upload
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(getContext(), "Iniciando upload...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");

                        // 1. Carrega a nova imagem no Perfil
                        Glide.with(requireContext())
                                .load(url)
                                .into(profileImage);


                       // saveUserImageUrlToDatabase(url);

                        Toast.makeText(getContext(), "Upload e atualização de perfil com sucesso!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(getContext(), "Erro no upload: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch(requireContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (photoUri != null) {
            outState.putString("txt_uri", photoUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString("txt_uri");
            if (uriString != null) {
                photoUri = Uri.parse(uriString);
            }
        }
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