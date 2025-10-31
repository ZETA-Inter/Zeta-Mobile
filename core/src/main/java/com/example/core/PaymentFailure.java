package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.core.databinding.FragmentPaymentFailureBinding;
import com.example.core.ui.BrandingHelper;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentFailure extends Fragment {

    private FragmentPaymentFailureBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private TipoUsuario tipoAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Resolve tipo e aplica theme overlay
        Bundle args = getArguments();
        TipoUsuario fromBundle = (args != null)
                ? (TipoUsuario) args.getSerializable("TIPO_USUARIO")
                : null;
        tipoAtual = BrandingHelper.resolveTipo(requireContext(), fromBundle);
        if (tipoAtual == null) tipoAtual = TipoUsuario.COMPANY; // fallback seguro

        LayoutInflater themed = BrandingHelper.themedInflater(requireContext(), inflater, tipoAtual);
        binding = FragmentPaymentFailureBinding.inflate(themed, container, false);
        View root = binding.getRoot();

        // Aplica logo (se houver no layout)
        BrandingHelper.applyBrandToViews(root, tipoAtual, R.id.imgLogo);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Dados do bundle (senha pode vir vazia em login social)
        String password = (args != null) ? args.getString("password", "") : "";
        String collection = (tipoAtual == TipoUsuario.COMPANY) ? "Fornecedor" : "Produtor";

        // Tenta remover dados/conta
        deleteUserData(collection, password, root);

        // Após ~2.5s, volta para Login
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                Navigation.findNavController(root).navigate(R.id.Login);
            }
        }, 2500);

        return root;
    }

    private void deleteUserData(@NonNull String collection,
                                @Nullable String password,
                                @NonNull View navView) {

        if (mAuth.getCurrentUser() == null) {
            // Nada para deletar; apenas limpa sessão e sai
            clearSession();
            Toast.makeText(requireContext(), "Sessão finalizada.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String uid = mAuth.getCurrentUser().getUid();
        final String email = mAuth.getCurrentUser().getEmail();

        // Se temos email + password, tentamos reautenticar para garantir permissão de deleção
        if (email != null && password != null && !password.trim().isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            mAuth.getCurrentUser().reauthenticate(credential)
                    .addOnSuccessListener(authResult -> {
                        // 1) Apaga documento no Firestore
                        mFirestore.collection(collection).document(uid).delete()
                                .addOnCompleteListener(task1 -> {
                                    // 2) Tenta deletar a conta do Auth
                                    deleteAuthUser();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PaymentFailure", "Reautenticação falhou: " + e.getMessage(), e);
                        Toast.makeText(requireContext(),
                                "Erro de autenticação. Não foi possível remover a conta agora.",
                                Toast.LENGTH_SHORT).show();
                        // Mesmo sem reauth, limpamos a sessão para segurança
                        clearSession();
                        mAuth.signOut();
                    });
        } else {
            // Sem senha (ex.: Google), tentar deletar Firestore; deleção do Auth pode exigir reauth recente
            mFirestore.collection(collection).document(uid).delete()
                    .addOnCompleteListener(task -> {
                        deleteAuthUser(); // pode falhar por falta de reauth; tratamos no listener
                    });
        }
    }

    private void deleteAuthUser() {
        if (mAuth.getCurrentUser() == null) {
            clearSession();
            return;
        }
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Conta removida por falta de pagamento.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = (task.getException() != null) ? task.getException().getMessage() : "desconhecido";
                        Log.e("PaymentFailure", "Erro ao remover conta: " + msg);
                        Toast.makeText(requireContext(),
                                "Erro ao remover conta: " + msg,
                                Toast.LENGTH_SHORT).show();
                    }
                    clearSession();
                    mAuth.signOut();
                });
    }

    private void clearSession() {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        } catch (Exception e) {
            Log.w("PaymentFailure", "Falha ao limpar sessão: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
