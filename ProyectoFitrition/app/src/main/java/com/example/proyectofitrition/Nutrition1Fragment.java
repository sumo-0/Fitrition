package com.example.proyectofitrition;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.proyectofitrition.api.SupabaseManager;
import com.example.proyectofitrition.models.Meal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Nutrition1Fragment extends Fragment {

    // Variables de agua (tu código existente)
    private ImageView[] waterGlasses = new ImageView[10];
    private boolean[] isGlassFull = new boolean[10];
    private TextView waterCountText;
    private int glassesDrankCount = 0;
    private LottieAnimationView lottieCelebration;
    private static final String PREFS_NAME = "FitritionWaterPrefs";
    private static final String KEY_LAST_DATE = "last_water_date";

    // NUEVO: Variables para el historial de comidas
    private RecyclerView recyclerMeals;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private SupabaseManager supabaseManager;
    private TextView tvEmptyMeals;
    private TextView tvTotalCalories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition1, container, false);

        supabaseManager = new SupabaseManager(requireContext());

        // Referencias de botones
        Button btnAddFoodCamera = view.findViewById(R.id.btn_add_food_camera);
        Button btnAddFoodManual = view.findViewById(R.id.btn_add_food_manual);
        waterCountText = view.findViewById(R.id.water_count_text);
        lottieCelebration = view.findViewById(R.id.lottie_celebration);

        // Configurar animación de celebración
        lottieCelebration.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
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

        // NUEVO: Inicializar RecyclerView para historial de comidas
        recyclerMeals = view.findViewById(R.id.recycler_meals);
        tvEmptyMeals = view.findViewById(R.id.tv_empty_meals);
        tvTotalCalories = view.findViewById(R.id.tv_total_calories);

        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(mealList, getContext());
        recyclerMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMeals.setAdapter(mealAdapter);

        // Cargar comidas desde Supabase
        if (supabaseManager.isUserLoggedIn()) {
            cargarComidas();
        } else {
            tvEmptyMeals.setText("Inicia sesión para ver tus comidas");
            tvEmptyMeals.setVisibility(View.VISIBLE);
        }

        return view;
    }

    // ==================== LÓGICA DE AGUA (TU CÓDIGO EXISTENTE) ====================

    private void initializeWaterGlasses(View view) {
        int[] glassIds = {
                R.id.water_glass_1, R.id.water_glass_2, R.id.water_glass_3, R.id.water_glass_4,
                R.id.water_glass_5, R.id.water_glass_6, R.id.water_glass_7, R.id.water_glass_8,
                R.id.water_glass_9, R.id.water_glass_10
        };

        for (int i = 0; i < 10; i++) {
            final int index = i;
            waterGlasses[index] = view.findViewById(glassIds[index]);

            if (isGlassFull[index]) {
                waterGlasses[index].setImageResource(R.drawable.ic_water_full);
            } else {
                waterGlasses[index].setImageResource(R.drawable.ic_water_empty);
            }

            waterGlasses[index].setOnClickListener(v -> handleGlassClick(index));
        }
    }

    private void handleGlassClick(int index) {
        if (!isGlassFull[index]) {
            isGlassFull[index] = true;
            waterGlasses[index].setImageResource(R.drawable.ic_water_full);
            glassesDrankCount++;

            // Animación de llenado (si la tienes)
            // waterGlasses[index].startAnimation(fillAnimation);

            // Celebración al completar 10 vasos
            if (glassesDrankCount == 10) {
                lottieCelebration.setVisibility(View.VISIBLE);
                lottieCelebration.playAnimation();
            }
        } else {
            isGlassFull[index] = false;
            waterGlasses[index].setImageResource(R.drawable.ic_water_empty);
            glassesDrankCount--;
        }
        updateWaterCountText();
        saveWaterState();
    }

    private void updateWaterCountText() {
        waterCountText.setText(glassesDrankCount + " / 10 vasos (Meta: 2.5L)");
    }

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
            for (int i = 0; i < 10; i++) {
                isGlassFull[i] = false;
            }
            saveWaterState();
        } else {
            for (int i = 0; i < 10; i++) {
                isGlassFull[i] = prefs.getBoolean("glass_" + i, false);
                if (isGlassFull[i]) {
                    glassesDrankCount++;
                }
            }
        }
    }

    // ==================== LÓGICA DE COMIDAS (NUEVO CON SUPABASE) ====================

    private void cargarComidas() {
        supabaseManager.getUserMeals(new SupabaseManager.MealsCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mealList.clear();
                        mealList.addAll(meals);

                        // Calcular total de calorías
                        int totalCalories = 0;
                        for (Meal meal : meals) {
                            totalCalories += meal.getCalories();
                        }

                        mealAdapter.notifyDataSetChanged();

                        // Actualizar UI
                        if (mealList.isEmpty()) {
                            tvEmptyMeals.setVisibility(View.VISIBLE);
                            recyclerMeals.setVisibility(View.GONE);
                            tvTotalCalories.setText("Total: 0 kcal");
                        } else {
                            tvEmptyMeals.setVisibility(View.GONE);
                            recyclerMeals.setVisibility(View.VISIBLE);
                            tvTotalCalories.setText("Total del día: " + totalCalories + " kcal");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al cargar comidas: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar comidas cuando volvemos al fragment
        if (supabaseManager != null && supabaseManager.isUserLoggedIn()) {
            cargarComidas();
        }
    }
}