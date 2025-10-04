package com.example.core;
import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.core.R;
import com.example.core.Repository;
import com.example.core.Validators; // Importação adicionada
import com.example.core.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends Fragment {

    private FragmentLoginBinding binding;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository();
    private static final int RC_GOOGLE = 9001;
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

        AuthAdapter adapter = new AuthAdapter();

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(requireActivity(), gso);

        // --------------------- login com email e senha ---------------------

        Bundle bundle = getArguments();

        if (bundle != null) {
            tipoAtual = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO");
        }
        else if (tipoAtual == null) {
            // Lidar com erro: se o tipo não foi passado, volte ou use um padrão.
            // Aqui, vamos apenas assumir que não pode ser nulo para o exemplo.
            Toast.makeText(requireContext(), "Tipo de usuário não informado", Toast.LENGTH_SHORT).show();
        }

        String email = String.valueOf(binding.tilEmail);
        String senha = String.valueOf(binding.tilSenha);


        // Listeners
        binding.btnEntrar.setOnClickListener(v -> {
            try {
                adapter.login(tipoAtual, email, senha, requireContext());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        binding.tvForgotPasswordCompany.setOnClickListener(v -> navigateToForgotPassword(v));
        if (binding.btnGoogle != null) {
            binding.btnGoogle.setOnClickListener(v -> startGoogleFlow());
        }

        binding.tvCadastro.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.Register, bundle);
        });
    }

    // ===== Google =====
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
                AuthCredential cred = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
                mAuth.signInWithCredential(cred)
                        .addOnSuccessListener(r -> {
                            // repo.upsertFromAuth(r.getUser(), null)
                        })
                        .addOnFailureListener(e -> error(binding.tilEmail, "Falha no Google Sign-In (verifique SHA-1/SHA-256 e default_web_client_id)"));
            } catch (ApiException e) {
                // Tratamento de erro ou cancelamento
            }
        }
    }

    // ===== Navegação e Utils UI =====
    private void navigateToForgotPassword(View v) {
        Navigation.findNavController(v).navigate(R.id.ForgetPassword);
    }

    private String get(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
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

    private String mapAuthLoginError(Exception e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("no user record")) return "Usuário não encontrado";
        if (m.contains("password is invalid")) return "Senha inválida";
        if (m.contains("INVALID_EMAIL")) return "E-mail inválido";
        return "Falha no login";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}