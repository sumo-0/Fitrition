package com.example.proyectofitrition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // Variables para pasos
    private TextView textStepsCount;
    private Button btnAddSteps;
    private int stepsCount = 0;

    // Persistencia
    private static final String PREFS_NAME = "FitritionStatsPrefs";
    private static final String KEY_STEPS = "steps_count";
    private static final String KEY_DATE = "last_steps_date";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- 1. Referencias UI ---
        CardView cardWorkout = view.findViewById(R.id.card_quick_workout);
        CardView cardNutrition = view.findViewById(R.id.card_quick_nutrition);
        textStepsCount = view.findViewById(R.id.text_steps_count);
        btnAddSteps = view.findViewById(R.id.btn_add_steps);

        // --- 2. Cargar datos de pasos ---
        loadStepsData();

        // --- 3. Lógica del botón de pasos ---
        btnAddSteps.setOnClickListener(v -> {
            stepsCount += 500;
            updateStepsUI();
            saveStepsData();
            // Feedback simple
            Toast.makeText(getContext(), "+500 Pasos", Toast.LENGTH_SHORT).show();
        });

        // --- 4. Animaciones de entrada (Código existente) ---
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        Animation animDelay = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        animDelay.setStartOffset(150);

        cardWorkout.startAnimation(anim);
        cardNutrition.startAnimation(animDelay);

        // --- 5. Navegación (Código existente) ---
        cardWorkout.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragment_container, new WorkoutsFragment())
                    .addToBackStack(null)
                    .commit();
            actualizarBottomNav(R.id.nav_workouts);
        });

        cardNutrition.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragment_container, new NutritionFragment())
                    .addToBackStack(null)
                    .commit();
            actualizarBottomNav(R.id.nav_nutrition);
        });

        return view;
    }

    // Método para actualizar el texto en pantalla
    private void updateStepsUI() {
        // Formato con separador de miles (ej: 1,500)
        textStepsCount.setText(String.format(Locale.getDefault(), "%,d", stepsCount));
    }

    // --- Persistencia de Datos ---

    private void saveStepsData() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Guardar fecha actual para detectar nuevo día
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        editor.putInt(KEY_STEPS, stepsCount);
        editor.putString(KEY_DATE, today);
        editor.apply();
    }

    private void loadStepsData() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String lastDate = prefs.getString(KEY_DATE, "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Si la fecha guardada no es la de hoy, reiniciamos a 0
        if (!lastDate.equals(today)) {
            stepsCount = 0;
            saveStepsData(); // Guardar el reinicio
        } else {
            stepsCount = prefs.getInt(KEY_STEPS, 0);
        }
        updateStepsUI();
    }

    private void actualizarBottomNav(int itemId) {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(itemId);
            }
        }
    }
}