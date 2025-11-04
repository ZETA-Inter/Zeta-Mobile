package com.example.feature_fornecedor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    public SettingsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        LinearLayout rowNotifications = view.findViewById(R.id.rowNotifications);
        LinearLayout rowLanguage     = view.findViewById(R.id.rowLanguage);
        LinearLayout rowHelp         = view.findViewById(R.id.rowHelp);
        MaterialButton btnLogout     = view.findViewById(R.id.btnLogout);

        // Voltar para a tela anterior
        btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        rowNotifications.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Notificações (em breve)", Toast.LENGTH_SHORT).show()
        );

        rowLanguage.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Idioma (em breve)", Toast.LENGTH_SHORT).show()
        );

        rowHelp.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ajuda (em breve)", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> doLogoutAndGoFirstPage());
    }

    private void doLogoutAndGoFirstPage() {
        // 1) Firebase signout
        try { FirebaseAuth.getInstance().signOut(); } catch (Exception ignored) {}

        // 2) Limpa a sessão local
        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        // 3) Navega para a FirstPage (módulo core) e limpa backstack
        NavController nav = NavHostFragment.findNavController(this);

        // Se você tiver um deep link cadastrado para a FirstPage:
        Uri firstPage = Uri.parse("app://Core/FirstPage");
        try {
            nav.popBackStack(nav.getGraph().getStartDestinationId(), false);
            nav.navigate(firstPage);
        } catch (Exception e) {
            // Fallback: caso FirstPage seja uma Activity do módulo core:
            try {
                Class<?> firstPageActivity = Class.forName("com.example.core.ui.FirstPage");
                Intent i = new Intent(requireContext(), firstPageActivity);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                requireActivity().finish();
            } catch (ClassNotFoundException ex) {
                Toast.makeText(requireContext(), "Não foi possível abrir a FirstPage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
