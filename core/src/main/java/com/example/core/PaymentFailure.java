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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentFailure extends Fragment {

    // A classe de binding gerada para o seu layout
    private FragmentPaymentFailureBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando o View Binding
        binding = FragmentPaymentFailureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle bundle = getArguments();

        String password = bundle.getString("password", "");

        String collection = (TipoUsuario) bundle.getSerializable("TIPO_USUARIO") == TipoUsuario.COMPANY ? "Fornecedor" : "Produtor";

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        deleteUserData(collection, password);

        // Espera ~2.5s e volta para a tela de planos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Navigation.findNavController(root).navigate(R.id.Login);

        }, 2500);

        return root;
    }

    private void deleteUserData(String collection, String password) {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // 1. Reautenticação
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(authResult -> {
                    mFirestore.collection(collection).document(uid).delete();

                    mAuth.getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                                prefs.edit().clear().apply();

                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Conta removida por falta de pagamento.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Erro ao remover conta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                mAuth.signOut();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erro de autenticação. Não foi possível remover a conta.", Toast.LENGTH_SHORT).show();
                });
    }



    // Importante: limpa a referência do binding para evitar vazamento de memória
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
