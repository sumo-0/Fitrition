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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Nutrition1Fragment extends Fragment {

    // Array para guardar las referencias a los 10 vasos
    private ImageView[] waterGlasses = new ImageView[10];
    // Array para saber si un vaso está lleno o no
    private boolean[] isGlassFull = new boolean[10];
    private TextView waterCountText;
    private int glassesDrankCount = 0;
    private Animation fillAnimation;

    // Constantes para SharedPreferences
    private static final String PREFS_NAME = "FitritionWaterPrefs";
    private static final String KEY_LAST_DATE = "last_water_date";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition1, container, false);

        // --- Tu código existente de botones ---
        Button btnAddFoodCamera = view.findViewById(R.id.btn_add_food_camera);
        Button btnAddFoodManual = view.findViewById(R.id.btn_add_food_manual);

        btnAddFoodCamera.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NutritionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAddFoodManual.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Función manual próximamente", Toast.LENGTH_SHORT).show();
        });
        // ---------------------------------------

        // === Lógica del Registro de Agua ===
        waterCountText = view.findViewById(R.id.water_count_text);
        fillAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.water_fill_anim);

        // Cargar el estado guardado del día
        loadWaterState();

        // Inicializar los vasos y sus listeners
        initializeWaterGlasses(view);

        // Actualizar el texto inicial
        updateWaterCountText();

        return view;
    }

    private void initializeWaterGlasses(View view) {
        // IDs de los vasos en el XML
        int[] glassIds = {
                R.id.water_glass_1, R.id.water_glass_2, R.id.water_glass_3, R.id.water_glass_4,
                R.id.water_glass_5, R.id.water_glass_6, R.id.water_glass_7, R.id.water_glass_8,
                R.id.water_glass_9, R.id.water_glass_10
        };

        for (int i = 0; i < 10; i++) {
            final int index = i;
            waterGlasses[index] = view.findViewById(glassIds[index]);

            // Establecer la imagen correcta según el estado cargado
            if (isGlassFull[index]) {
                waterGlasses[index].setImageResource(R.drawable.ic_water_full);
            } else {
                waterGlasses[index].setImageResource(R.drawable.ic_water_empty);
            }

            // Configurar el clic
            waterGlasses[index].setOnClickListener(v -> handleGlassClick(index));
        }
    }

    private void handleGlassClick(int index) {
        if (!isGlassFull[index]) {
            // Si está vacío, llenarlo
            isGlassFull[index] = true;
            waterGlasses[index].setImageResource(R.drawable.ic_water_full);
            waterGlasses[index].startAnimation(fillAnimation); // Reproducir animación
            glassesDrankCount++;
        } else {
            // Si está lleno, vaciarlo (por si el usuario se equivoca)
            isGlassFull[index] = false;
            waterGlasses[index].setImageResource(R.drawable.ic_water_empty);
            glassesDrankCount--;
        }
        updateWaterCountText();
        saveWaterState(); // Guardar el nuevo estado
    }

    private void updateWaterCountText() {
        waterCountText.setText(glassesDrankCount + " / 10 vasos (Meta: 2.5L)");
    }

    // --- Métodos de Persistencia (SharedPreferences) ---

    private void saveWaterState() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Guardar la fecha de hoy
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(KEY_LAST_DATE, today);

        // Guardar el estado de cada vaso
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
            // Si es un nuevo día, resetear todo
            for (int i = 0; i < 10; i++) {
                isGlassFull[i] = false;
            }
            saveWaterState(); // Guardar el estado reseteado
        } else {
            // Si es el mismo día, cargar el estado
            for (int i = 0; i < 10; i++) {
                isGlassFull[i] = prefs.getBoolean("glass_" + i, false);
                if (isGlassFull[i]) {
                    glassesDrankCount++;
                }
            }
        }
    }
}