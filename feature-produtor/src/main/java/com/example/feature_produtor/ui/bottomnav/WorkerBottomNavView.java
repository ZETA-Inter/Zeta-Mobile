package com.example.feature_produtor.ui.bottomnav;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;

import com.example.feature_produtor.R;

public class WorkerBottomNavView extends FrameLayout {

    public enum Item { LESSONS, HOME, GOALS }

    private ImageView btnLessons, btnHome, btnGoals;

    @Nullable private NavController navController;
    private int destLessons, destHome, destGoals;
    private Item current = Item.HOME;

    // PNGs no /res/drawable (troque os nomes pelos seus arquivos)
    private final int LESSONS_OUTLINE = R.drawable.ic_lessons_outline;  // ex.: trofeu_contorno.png
    private final int LESSONS_FILLED = R.drawable.ic_lessons_filled;   // ex.: trofeu_preenchido.png

    private final int HOME_OUTLINE   = R.drawable.ic_home_outline;
    private final int HOME_FILLED    = R.drawable.ic_home_filled;

    private final int GOALS_OUTLINE = R.drawable.ic_goals_outline;
    private final int GOALS_FILLED    = R.drawable.ic_goals_filled;


    public WorkerBottomNavView(Context c) { this(c, null); }
    public WorkerBottomNavView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        init();
    }

    private void init() {
        // Layout simplificado: só a pílula azul e 3 ícones
        inflate(getContext(), R.layout.view_bottom_nav_worker, this);

        btnLessons = findViewById(R.id.btnLessons);
        btnHome   = findViewById(R.id.btnHome);
        btnGoals   = findViewById(R.id.btnGoals);

        btnLessons.setOnClickListener(v -> setActive(Item.LESSONS, true));
        btnHome.setOnClickListener(v   -> setActive(Item.HOME,   true));
        btnGoals.setOnClickListener(v   -> setActive(Item.GOALS,   true));

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
    public void bindNavController(NavController nav, int lessonsDestId, int homeDestId, int goalsDestId) {
        this.navController = nav;
        this.destLessons = lessonsDestId;
        this.destHome   = homeDestId;
        this.destGoals   = goalsDestId;
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
        btnLessons.setImageResource(selected == Item.LESSONS ? LESSONS_FILLED : LESSONS_OUTLINE);
        btnLessons.setSelected(selected == Item.LESSONS);

        // home
        btnHome.setImageResource(selected == Item.HOME ? HOME_FILLED : HOME_OUTLINE);
        btnHome.setSelected(selected == Item.HOME);

        // team
        btnGoals.setImageResource(selected == Item.GOALS ? GOALS_FILLED : GOALS_OUTLINE);
        btnGoals.setSelected(selected == Item.GOALS);
    }

    private void navigateTo(Item item) {
        if (navController == null) return;
        int dest = (item == Item.LESSONS) ? destLessons : (item == Item.GOALS) ? destGoals : destHome;
        if (dest == 0) return;
        try {
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != dest) {
                navController.navigate(dest);
            }
        } catch (Exception ignore) {}
    }
}
