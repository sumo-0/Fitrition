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
import java.util.Random;

public class HomeFragment extends Fragment {

    // Variables Pasos
    private TextView textStepsCount;
    private Button btnAddSteps;
    private int stepsCount = 0;

    // Variables Motivación
    private TextView textQuote;
    private Button btnNewQuote;
    private final String[] quotes = {
            "El único mal entrenamiento es el que no ha ocurrido.",
            "Tu cuerpo puede aguantar casi todo. Es a tu mente a la que tienes que convencer.",
            "No pares cuando estés cansado, para cuando hayas terminado.",
            "La disciplina es hacer lo que hay que hacer, aunque no quieras hacerlo.",
            "El dolor que sientes hoy es la fuerza que sentirás mañana.",
            "Una hora de ejercicio es solo el 4% de tu día. No hay excusas.",
            "No te compares con los demás. Compárate con la persona que eras ayer.",
            "La motivación es lo que te pone en marcha. El hábito es lo que hace que sigas.",
            "El sudor es la grasa llorando.",
            "No sueñes con ello, trabaja por ello.",
            "Cada paso cuenta, incluso los pequeños."
    };

    // Preferencias
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

        // Referencias Motivación
        textQuote = view.findViewById(R.id.text_quote);
        btnNewQuote = view.findViewById(R.id.btn_new_quote);

        // --- 2. Cargar datos de pasos ---
        loadStepsData();

        // --- 3. Lógica Pasos ---
        btnAddSteps.setOnClickListener(v -> {
            stepsCount += 500;
            updateStepsUI();
            saveStepsData();
            Toast.makeText(getContext(), "+500 Pasos", Toast.LENGTH_SHORT).show();
        });

        // --- 4. Lógica Motivación ---
        setRandomQuote(); // Frase inicial
        btnNewQuote.setOnClickListener(v -> {
            // Pequeña animación al cambiar texto
            textQuote.animate().alpha(0f).setDuration(150).withEndAction(() -> {
                setRandomQuote();
                textQuote.animate().alpha(1f).setDuration(150);
            });
        });

        // --- 5. Animaciones de entrada ---
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        Animation animDelay = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        animDelay.setStartOffset(150);

        cardWorkout.startAnimation(anim);
        cardNutrition.startAnimation(animDelay);

        // --- 6. Navegación ---
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

    private void setRandomQuote() {
        int randomIndex = new Random().nextInt(quotes.length);
        textQuote.setText("\"" + quotes[randomIndex] + "\"");
    }

    private void updateStepsUI() {
        textStepsCount.setText(String.format(Locale.getDefault(), "%,d", stepsCount));
    }

    private void saveStepsData() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

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

        if (!lastDate.equals(today)) {
            stepsCount = 0;
            saveStepsData();
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