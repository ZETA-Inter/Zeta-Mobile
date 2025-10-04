package com.example.core;

<<<<<<< HEAD
import android.content.Intent;
=======
import android.net.Uri;
>>>>>>> d79c11af5e5f5c2d95ef04002cc068c4d1b7de6d
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.core.TipoUsuario;

import com.example.core.databinding.FragmentFirstPageBinding;

public class FirstPage extends Fragment {

    private FragmentFirstPageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Navegação para o LoginFragment

<<<<<<< HEAD

        binding.btnFornecedor.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("TIPO_USUARIO", TipoUsuario.COMPANY);
            // Use o ID do fragmento de destino que foi definido no nav_core.xml
            Navigation.findNavController(v).navigate(R.id.Login, bundle);
        });

        binding.btnProdutor.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("TIPO_USUARIO", TipoUsuario.WORKER);
            // Use o ID do fragmento de destino que você definiu no nav_core.xml
            Navigation.findNavController(v).navigate(R.id.Login, bundle);
=======
        binding.btnProdutor.setOnClickListener(v -> {
            // Use o ID do fragmento de destino que você definiu no nav_core.xml
//                Navigation.findNavController(v).navigate(R.id.Login)

            Uri deeplink = Uri.parse("app://Worker/Lessons");
            Navigation.findNavController(v).navigate(deeplink);
>>>>>>> d79c11af5e5f5c2d95ef04002cc068c4d1b7de6d
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}