package com.example.feature_fornecedor.ui.bottomnav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;

import com.example.feature_fornecedor.R;

public class CompanyBottomNavView extends FrameLayout {

    public enum Item { AWARDS, HOME, TEAM }

    private ImageView btnAwards, btnHome, btnTeam;

    @Nullable private NavController navController;
    private int destAwards, destHome, destTeam;
    private Item current = Item.HOME;

    // PNGs no /res/drawable (troque os nomes pelos seus arquivos)
    private final int AWARDS_OUTLINE = R.drawable.ic_awards_outline;  // ex.: trofeu_contorno.png
    private final int AWARDS_FILLED  = R.drawable.ic_awards_filled;   // ex.: trofeu_preenchido.png

    private final int HOME_OUTLINE   = R.drawable.ic_home_outline;
    private final int HOME_FILLED    = R.drawable.ic_home_filled;

    private final int TEAM_OUTLINE   = R.drawable.ic_team_outline;
    private final int TEAM_FILLED    = R.drawable.ic_team_filled;

    public CompanyBottomNavView(Context c) { this(c, null); }
    public CompanyBottomNavView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        init();
    }

    private void init() {
        // Layout simplificado: só a pílula azul e 3 ícones
        inflate(getContext(), R.layout.view_bottom_nav_company, this);

        btnAwards = findViewById(R.id.btnAwards);
        btnHome   = findViewById(R.id.btnHome);
        btnTeam   = findViewById(R.id.btnTeam);

        btnAwards.setOnClickListener(v -> setActive(Item.AWARDS, true));
        btnHome.setOnClickListener(v   -> setActive(Item.HOME,   true));
        btnTeam.setOnClickListener(v   -> setActive(Item.TEAM,   true));

        // Acomoda os system insets (gesture/nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            final int bottom = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            ).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return insets;
        });

        // estado inicial
        applyIcons(Item.HOME);
    }

    /** Integra com o NavController (opcional). */
    public void bindNavController(NavController nav, int awardsDestId, int homeDestId, int teamDestId) {
        this.navController = nav;
        this.destAwards = awardsDestId;
        this.destHome   = homeDestId;
        this.destTeam   = teamDestId;
    }

    /** Troca a aba ativa; se navigate=true, navega após trocar o ícone. */
    public void setActive(Item item, boolean navigate) {
        if (current == item) {
            if (navigate) navigateTo(item);
            return;
        }
        current = item;
        applyIcons(item);
        if (navigate) navigateTo(item);
    }

    /** Use quando quiser só refletir a rota atual sem navegar. */
    public void setActive(Item item) {
        setActive(item, false);
    }

    private void applyIcons(Item selected) {
        // awards
        btnAwards.setImageResource(selected == Item.AWARDS ? AWARDS_FILLED : AWARDS_OUTLINE);
        btnAwards.setSelected(selected == Item.AWARDS);

        // home
        btnHome.setImageResource(selected == Item.HOME ? HOME_FILLED : HOME_OUTLINE);
        btnHome.setSelected(selected == Item.HOME);

        // team
        btnTeam.setImageResource(selected == Item.TEAM ? TEAM_FILLED : TEAM_OUTLINE);
        btnTeam.setSelected(selected == Item.TEAM);
    }

    private void navigateTo(Item item) {
        if (navController == null) return;
        int dest = (item == Item.AWARDS) ? destAwards : (item == Item.TEAM) ? destTeam : destHome;
        if (dest == 0) return;
        try {
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != dest) {
                navController.navigate(dest);
            }
        } catch (Exception ignore) {}
    }
}
