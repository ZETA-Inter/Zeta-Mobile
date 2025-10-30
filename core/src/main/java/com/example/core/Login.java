package com.example.core;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends Fragment {

    private static final int RC_GOOGLE = 9001;

    private FragmentLoginBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository();
    private GoogleSignInClient gsc;
    private TipoUsuario tipoAtual; // se for COMPANY, company_id = uid; para WORKER, ajuste depois conforme sua lógica

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // O AuthAdapter já está instanciado como campo de classe (this.adapter), podemos remover esta linha.
        // AuthAdapter adapter = new AuthAdapter();

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(requireActivity(), gso);

        // --------------------- Definição do Tipo de Usuário ---------------------

        // Recupera tipo de usuário
        Bundle bundle = getArguments();
        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        } else if (tipoAtual == null) {
            Toast.makeText(requireContext(), "Tipo de usuário não informado", Toast.LENGTH_SHORT).show();
        }

        // ===== Login por e-mail/senha (direto com FirebaseAuth para encadear upsert + sessão)
        binding.btnEntrar.setOnClickListener(v -> {
            String email = binding.tilEmail.getEditText() != null ? binding.tilEmail.getEditText().getText().toString().trim() : "";
            String senha = binding.tilSenha.getEditText() != null ? binding.tilSenha.getEditText().getText().toString().trim() : "";

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
                        // 1) upsert no Firestore (company/{uid})
                        repo.upsertFromAuth(result.getUser(), null)
                                .addOnSuccessListener(aVoid -> {
                                    // 2) salvar sessão com company_id = uid (para COMPANY)
                                    String uid = result.getUser().getUid();
                                    salvarSessaoBasica(uid, result.getUser().getEmail(), result.getUser().getDisplayName());

                                    // 3) navegar
                                    navegarDepoisLogin(v);
                                    bloquearUI(false);
                                })
                                .addOnFailureListener(e -> {
                                    bloquearUI(false);
                                    mostrarMensagem("Erro ao atualizar perfil: " + e.getMessage());
                                });
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

    // ===== Google Sign-In + FirebaseAuth =====
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
                    error(binding.tilEmail, "ID Token nulo (verifique default_web_client_id, SHA e Play Services).");
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
                            // 1) upsert no Firestore (company/{uid})
                            repo.upsertFromAuth(r.getUser(), null)
                                    .addOnSuccessListener(aVoid -> {
                                        // 2) salvar sessão com company_id = uid (para COMPANY)
                                        String uid = r.getUser().getUid();
                                        salvarSessaoBasica(uid, r.getUser().getEmail(), r.getUser().getDisplayName());

                                        // 3) navegar
                                        navegarDepoisLogin(requireView());
                                        bloquearUI(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        bloquearUI(false);
                                        mostrarMensagem("Erro ao atualizar perfil: " + e.getMessage());
                                    });
                        })
                        .addOnFailureListener(e -> {
                            bloquearUI(false);
                            error(binding.tilEmail, "Falha no Google Sign-In: " + (e.getMessage() != null ? e.getMessage() : ""));
                        });

            } catch (ApiException e) {
                mostrarMensagem("Login com Google cancelado ou falhou.");
            }
        }
    }

    // ===== Helpers: sessão + navegação =====

    /** Salva sessão básica e garante company_id = uid (coerente com Repository que escreve em 'company/{uid}'). */
    private void salvarSessaoBasica(@NonNull String uid, @Nullable String email, @Nullable String nome) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE);
        sp.edit()
                .putString("company_id", uid)                   // <== chave que a sua tela de lista usa
                .putString("email", email != null ? email : "")
                .putString("name", nome != null ? nome : "")
                .putString("tipo_usuario", tipoAtual != null ? tipoAtual.name() : "")
                .apply();
    }

    /** Decide deeplink por tipo e navega. */
    private void navegarDepoisLogin(@NonNull View navView) {
        String deeplink = (tipoAtual == TipoUsuario.WORKER) ? "app://Worker/Home" : "app://Company/Home";
        Uri deepLinkUri = Uri.parse(deeplink);
        Navigation.findNavController(navView).navigate(deepLinkUri);
    }

    // ===== Validação e UI =====
    private boolean validarCampos(String email, String senha) {
        boolean ok = true;
        clearErrors();

        if (TextUtils.isEmpty(email)) {
            error(binding.tilEmail, "Informe seu e-mail");
            ok = false;
        } else if (!Validators.isValidEmail(email)) {
            error(binding.tilEmail, "E-mail inválido");
            ok = false;
        }

        if (TextUtils.isEmpty(senha)) {
            error(binding.tilSenha, "Informe sua senha");
            ok = false;
        }
        return ok;
    }

    private void navigateToForgotPassword(View v) {
        Navigation.findNavController(v).navigate(R.id.ForgetPassword);
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
        // se tiver progress bar, exiba/oculte aqui
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}