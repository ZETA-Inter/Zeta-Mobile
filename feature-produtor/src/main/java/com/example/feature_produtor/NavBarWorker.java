package com.example.feature_produtor;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.feature_produtor.R;

public class NavBarWorker extends Fragment {

    private FrameLayout navAtividades, navHome, navMetas;
    private FrameLayout selectedItem;

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navAtividades = view.findViewById(R.id.nav_atividades_container);
        navHome = view.findViewById(R.id.nav_home_container);
        navMetas = view.findViewById(R.id.nav_metas_container);

        View.OnClickListener listener = v -> selectItem((FrameLayout) v);

        navAtividades.setOnClickListener(listener);
        navHome.setOnClickListener(listener);
        navMetas.setOnClickListener(listener);
    }

    private void selectItem(FrameLayout newItem) {
        if (selectedItem != null) {
            // Volta o anterior
            selectedItem.setBackground(null);
            ObjectAnimator.ofFloat(selectedItem, "translationY", 0f).start();
        }

        newItem.setBackgroundResource(R.drawable.nav_item_select);
        ObjectAnimator.ofFloat(newItem, "translationY", -20f).start();
        selectedItem = newItem;

        // Navegação por ID
        int id = newItem.getId();
        if (id == R.id.nav_home_container) {
            Navigation.findNavController(requireView()).navigate(R.id.HomePageWorker);
        } else if (id == R.id.nav_atividades_container) {
            Navigation.findNavController(requireView()).navigate(R.id.LessonsWorker);
        } else if (id == R.id.nav_metas_container) {
            Navigation.findNavController(requireView()).navigate(R.id.GoalsPageWorker);
        }
    }
}
