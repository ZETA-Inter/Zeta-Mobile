package com.example.core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.core.utils.IValidator;

import com.example.core.client.ApiPostgresClient; // Importação necessária
import com.example.core.databinding.FragmentLoginBinding;
import com.example.core.dto.response.WorkerResponse; // Importação necessária (DTO de resposta do Worker)
import com.example.core.network.RetrofitClientPostgres; // Importação necessária
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends Fragment {

    private static final int RC_GOOGLE = 9001;
    private static final String TAG = "LoginFragment"; // Tag para logs
    private static final String PREF_NAME = "user_session"; // Nome do SharedPreferences

    private FragmentLoginBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository();
    private GoogleSignInClient gsc;
    private TipoUsuario tipoAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configuração do Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // .requestIdToken(getString(R.string.default_web_client_id)) // Descomente se for usar token no backend
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(requireActivity(), gso);

        // --------------------- Definição do Tipo de Usuário ---------------------
        Bundle bundle = getArguments();
        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        }
        if (tipoAtual == null) {
            mostrarMensagem("Tipo de usuário não informado.");
            return;
        }

        // ===== Login por e-mail/senha
        binding.btnEntrar.setOnClickListener(v -> {
            String email = getEmail();
            String senha = getSenha();

            if (!validarCampos(email, senha)) {
                mostrarMensagem("Corrija os campos em destaque.");
                return;
            }

            bloquearUI(true);
            mAuth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(result -> {
                        if (result.getUser() == null) {
                            bloquearUI(false);
                            mostrarMensagem("Falha ao autenticar usuário.");
                            return;
                        }
                        // CHAMA O HANDLER UNIFICADO
                        handleAuthSuccess(result.getUser(), v);
                    })
                    .addOnFailureListener(e -> {
                        bloquearUI(false);
                        mostrarMensagem("Erro ao realizar login: " + (e.getMessage() != null ? e.getMessage() : ""));
                    });
        });

        // Esqueci a senha
        binding.tvForgotPasswordCompany.setOnClickListener(this::navigateToForgotPassword);

        // Google Sign-In
        if (binding.btnGoogle != null) {
            binding.btnGoogle.setOnClickListener(v -> startGoogleFlow());
        }

        // Cadastro
        binding.tvCadastro.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.Register, bundle);
        });
    }

    // ====================================================================
    // 🧰 FUNÇÕES PRINCIPAIS DE BUSCA E SESSÃO (WORKER ID)
    // ====================================================================

    /** * Roteia a lógica de sucesso do Firebase para a ação apropriada
     * (Buscar ID Postgres para Worker ou Salvar UID para Company).
     */
    private void handleAuthSuccess(@NonNull FirebaseUser user, @NonNull View navView) {
        String email = user.getEmail();
        String name = user.getDisplayName();
        String uid = user.getUid();

        if (email == null) {
            bloquearUI(false);
            mostrarMensagem("E-mail não encontrado na autenticação. Tente novamente.");
            return;
        }

        if (tipoAtual == TipoUsuario.WORKER) {
            // WORKER: Busca o ID do PostgreSQL por e-mail
            fetchWorkerIdByEmail(email, name, uid, navView);
        } else {
            // COMPANY: Salva o UID do Firebase como company_id no Firestore e localmente
            repo.upsertFromAuth(user, null)
                    .addOnSuccessListener(aVoid -> {
                        salvarSessaoCompany(uid, email, name);
                        navegarDepoisLogin(navView);
                        bloquearUI(false);
                    })
                    .addOnFailureListener(e -> {
                        bloquearUI(false);
                        mostrarMensagem("Erro ao atualizar perfil da Empresa: " + e.getMessage());
                    });
        }
    }


    /** * Busca o Worker ID no Backend do PostgreSQL usando o e-mail (endpoint /find-email/{email}).
     */
    private void fetchWorkerIdByEmail(@NonNull String email, @Nullable String name,
                                      @NonNull String firebaseUid, @NonNull View navView) {

        ApiPostgresClient api = RetrofitClientPostgres.getApiService(requireContext());

        api.findWorkerByEmail(email).enqueue(new Callback<WorkerResponse>() {
            @Override
            public void onResponse(@NonNull Call<WorkerResponse> call, @NonNull Response<WorkerResponse> response) {
                bloquearUI(false);
                if (response.isSuccessful() && response.body() != null) {
                    Integer workerId = response.body().getId();

                    if (workerId != null && workerId > 0) {
                        // SUCESSO! 💾 Salva o ID do PostgreSQL e navega.
                        salvarSessaoWorker(workerId, email, name, firebaseUid);
                        navegarDepoisLogin(navView);
                    } else {
                        // Worker encontrado, mas ID é inválido
                        mostrarMensagem("ID do Worker não encontrado no banco de dados. Contate o suporte.");
                    }

                } else {
                    // ERRO: Usuário existe no Firebase, mas não no PostgreSQL (404 ou 204)
                    Log.e(TAG, "Worker not found in Postgres for email: " + email);
                    mostrarMensagem("Worker não encontrado no banco de dados. Login falhou.");
                    mAuth.signOut(); // Limpa o Firebase para forçar novo cadastro limpo
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkerResponse> call, @NonNull Throwable t) {
                bloquearUI(false);
                Log.e(TAG, "Erro de conexão ao buscar Worker ID: " + t.getMessage(), t);
                mostrarMensagem("Erro de conexão ao buscar Worker. Verifique sua rede.");
            }
        });
    }

    /** Salva dados de sessão para WORKER, incluindo o ID do PostgreSQL. */
    private void salvarSessaoWorker(int workerId, @Nullable String email, @Nullable String nome, @NonNull String firebaseUid) {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putInt("worker_id", workerId)                         // <-- CHAVE DO WORKER (PostgreSQL ID)
                .putString("email", email != null ? email : "")
                .putString("name", nome != null ? nome : "")
                .putString("tipo_usuario", tipoAtual.name())
                .putString("firebase_uid", firebaseUid)                // Opcional: UID do Firebase
                .apply();
    }

    /** Salva dados de sessão para COMPANY (usa o UID do Firebase como company_id). */
    private void salvarSessaoCompany(@NonNull String uid, @Nullable String email, @Nullable String nome) {
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString("company_id", uid)                   // COMPANY: UID do Firebase
                .putString("email", email != null ? email : "")
                .putString("name", nome != null ? nome : "")
                .putString("tipo_usuario", tipoAtual.name())
                .apply();
    }

    // ====================================================================
    // 🌐 GOOGLE SIGN-IN FLOW
    // ====================================================================

    private void startGoogleFlow() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                String idToken = acc != null ? acc.getIdToken() : null;
                if (idToken == null) {
                    error(binding.tilEmail, "ID Token nulo (verifique configuração).");
                    return;
                }
                bloquearUI(true);

                AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
                mAuth.signInWithCredential(cred)
                        .addOnSuccessListener(r -> {
                            if (r.getUser() == null) {
                                bloquearUI(false);
                                mostrarMensagem("Falha ao obter usuário (Google).");
                                return;
                            }
                            // CHAMA O HANDLER UNIFICADO (Google)
                            handleAuthSuccess(r.getUser(), requireView());
                        })
                        .addOnFailureListener(e -> {
                            bloquearUI(false);
                            error(binding.tilEmail, "Falha no Google Sign-In: " + (e.getMessage() != null ? e.getMessage() : ""));
                        });

            } catch (ApiException e) {
                mostrarMensagem("Login com Google cancelado ou falhou.");
                bloquearUI(false);
            }
        }
    }

    // ====================================================================
    // 🧭 NAVEGAÇÃO, VALIDAÇÃO E UI
    // ====================================================================

    /** Decide deeplink por tipo e navega. */
    private void navegarDepoisLogin(@NonNull View navView) {
        String deeplink = (tipoAtual == TipoUsuario.WORKER) ? "app://Worker/Home" : "app://Company/Home";
        Uri deepLinkUri = Uri.parse(deeplink);
        Navigation.findNavController(navView).navigate(deepLinkUri);
    }

    private void navigateToForgotPassword(View v) {
        Navigation.findNavController(v).navigate(R.id.ForgetPassword);
    }

    private String getEmail() {
        return binding.tilEmail.getEditText() != null ? binding.tilEmail.getEditText().getText().toString().trim() : "";
    }

    private String getSenha() {
        return binding.tilSenha.getEditText() != null ? binding.tilSenha.getEditText().getText().toString().trim() : "";
    }

    private boolean validarCampos(String email, String senha) {
        boolean ok = true;
        clearErrors();

        if (TextUtils.isEmpty(email)) {
            error(binding.tilEmail, "Informe seu e-mail");
            ok = false;
        } else if (requireContext() instanceof IValidator && !((IValidator) requireContext()).isValidEmail(email)) {
            // Assumindo que você tem uma interface IValidator ou classe Validators
            error(binding.tilEmail, "E-mail inválido");
            ok = false;
        }

        if (TextUtils.isEmpty(senha)) {
            error(binding.tilSenha, "Informe sua senha");
            ok = false;
        }
        return ok;
    }

    private void clearErrors() {
        if (binding.tilEmail != null) binding.tilEmail.setError(null);
        if (binding.tilSenha != null) binding.tilSenha.setError(null);
        ocultarMensagem();
    }

    private void error(TextInputLayout til, String msg) {
        if (til == null) return;
        til.setError(msg);
        til.setErrorIconDrawable(null);
        int red = ContextCompat.getColor(requireContext(), R.color.error_red);
        til.setBoxStrokeColor(red);
        try {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
        } catch (Exception ignored) {}
    }

    private void mostrarMensagem(String s) {
        if (binding.tvFormMsg != null) {
            binding.tvFormMsg.setText(s);
            binding.tvFormMsg.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarMensagem() {
        if (binding.tvFormMsg != null) binding.tvFormMsg.setVisibility(View.GONE);
    }

    private void bloquearUI(boolean busy) {
        if (binding == null) return;
        if (binding.btnEntrar != null) binding.btnEntrar.setEnabled(!busy);
        if (binding.btnGoogle != null) binding.btnGoogle.setEnabled(!busy);
        // Exiba/oculte progress bar ou loading spinner aqui
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}