package com.example.proyectofitrition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Nutrition1Fragment extends Fragment {

    private ImageView[] waterGlasses = new ImageView[10];
    private boolean[] isGlassFull = new boolean[10];
    private TextView waterCountText;
    private int glassesDrankCount = 0;

    // Referencia al componente Lottie
    private LottieAnimationView lottieCelebration;

    private static final String PREFS_NAME = "FitritionWaterPrefs";
    private static final String KEY_LAST_DATE = "last_water_date";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition1, container, false);

        // Referencias
        Button btnAddFoodCamera = view.findViewById(R.id.btn_add_food_camera);
        Button btnAddFoodManual = view.findViewById(R.id.btn_add_food_manual);
        waterCountText = view.findViewById(R.id.water_count_text);
        lottieCelebration = view.findViewById(R.id.lottie_celebration);

        // Configurar listener para ocultar la animación cuando termine
        lottieCelebration.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                // Cuando termina el confeti, ocultamos la vista
                lottieCelebration.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        // Navegación
        btnAddFoodCamera.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragment_container, new NutritionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAddFoodManual.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Función manual próximamente", Toast.LENGTH_SHORT).show();
        });

        // Inicializar lógica de agua
        loadWaterState();
        initializeWaterGlasses(view);
        updateWaterCountText();

        return view;
    }

    private void initializeWaterGlasses(View view) {
        int[] glassIds = {
                R.id.water_glass_1, R.id.water_glass_2, R.id.water_glass_3, R.id.water_glass_4,
                R.id.water_glass_5, R.id.water_glass_6, R.id.water_glass_7, R.id.water_glass_8,
                R.id.water_glass_9, R.id.water_glass_10
        };

        for (int i = 0; i < 10; i++) {
            final int index = i;
            waterGlasses[index] = view.findViewById(glassIds[index]);

            // Establecer nivel inicial (sin animación al abrir la app)
            Drawable glassDrawable = waterGlasses[index].getDrawable();
            if (isGlassFull[index]) {
                glassDrawable.setLevel(10000); // Lleno
            } else {
                glassDrawable.setLevel(0);     // Vacío
            }

            waterGlasses[index].setOnClickListener(v -> handleGlassClick(index, v));
        }
    }

    private void handleGlassClick(int index, View v) {
        Drawable drawable = waterGlasses[index].getDrawable();

        if (!isGlassFull[index]) {
            // == LLENAR VASO ==
            isGlassFull[index] = true;

            // Animar el nivel de líquido (clip drawable)
            ObjectAnimator animator = ObjectAnimator.ofInt(drawable, "level", 0, 10000);
            animator.setDuration(600);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            // Vibración
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            glassesDrankCount++;

            // CHECK: ¿Hemos llegado a la meta?
            if (glassesDrankCount == 10) {
                playCelebration();
            }

        } else {
            // == VACIAR VASO ==
            isGlassFull[index] = false;

            ObjectAnimator animator = ObjectAnimator.ofInt(drawable, "level", 10000, 0);
            animator.setDuration(400);
            animator.start();

            glassesDrankCount--;
        }
        updateWaterCountText();
        saveWaterState();
    }

    private void playCelebration() {
        // Aseguramos que la vista es visible antes de reproducir
        lottieCelebration.setVisibility(View.VISIBLE);
        lottieCelebration.playAnimation();

        Toast.makeText(getContext(), "¡Hidratación completa! 💧🎉", Toast.LENGTH_LONG).show();
    }

    private void updateWaterCountText() {
        waterCountText.setText(glassesDrankCount + " / 10 vasos (Meta: 2.5L)");
    }

    // --- MÉTODOS DE GUARDADO (PERSISTENCIA) ---

    private void saveWaterState() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(KEY_LAST_DATE, today);

        for (int i = 0; i < 10; i++) {
            editor.putBoolean("glass_" + i, isGlassFull[i]);
        }
        editor.apply();
    }

    private void loadWaterState() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        glassesDrankCount = 0;

        if (!lastDate.equals(today)) {
            // Nuevo día: resetear todo
            for (int i = 0; i < 10; i++) isGlassFull[i] = false;
            saveWaterState();
        } else {
            // Mismo día: cargar estado
            for (int i = 0; i < 10; i++) {
                isGlassFull[i] = prefs.getBoolean("glass_" + i, false);
                if (isGlassFull[i]) glassesDrankCount++;
            }
        }
    }
}