package com.example.feature_fornecedor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.feature_fornecedor.ui.bottomnav.CompanyBottomNavView;

public class HomePageCompany extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomePageCompany() { }

    public static HomePageCompany newInstance(String param1, String param2) {
        HomePageCompany fragment = new HomePageCompany();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_page_company, container, false);

        CompanyBottomNavView bottom = v.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,   // troféu (awards)
                    R.id.HomePageCompany,      // home
                    R.id.WorkerListPageCompany // pessoas (team)
            );

            // marca a aba atual SEM navegar (apenas muda o ícone para preenchido)
            bottom.setActive(CompanyBottomNavView.Item.HOME, false);


        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ícone de perfil
        ImageView imgProfile = view.findViewById(R.id.imgProfile);

        imgProfile.setOnClickListener(v -> {
            SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
            int companyId = sp.getInt("user_id", -1);
            if (companyId <= 0) {
                Toast.makeText(requireContext(), "Perfil indisponível: ID do usuário não encontrado na sessão.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle args = new Bundle();
            args.putString("EXTRA_KIND", "COMPANY");
            args.putInt("EXTRA_ID", companyId);

            NavController nav = NavHostFragment.findNavController(this);
            nav.navigate(R.id.action_HomePageCompany_to_Profile, args);
        });





        CompanyBottomNavView bottom = view.findViewById(R.id.bottomNav);
        if (bottom != null) {
            NavController nav = NavHostFragment.findNavController(this);
            bottom.bindNavController(
                    nav,
                    R.id.RankingPageCompany,    // troféu
                    R.id.HomePageCompany,       // home
                    R.id.WorkerListPageCompany  // pessoas
            );
            // Marca a aba atual sem navegar
            bottom.setActive(CompanyBottomNavView.Item.HOME, false);
        }
    }

}
