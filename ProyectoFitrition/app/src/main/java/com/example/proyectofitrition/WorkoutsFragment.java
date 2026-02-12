package com.example.proyectofitrition;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutsFragment extends Fragment {

    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList;

    // Variables para el Timer
    private CountDownTimer restTimer;
    private boolean isTimerRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);

        recyclerView = view.findViewById(R.id.recycler_workouts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Crear datos de prueba útiles
        initializeWorkouts();

        // 2. Configurar el adaptador y el clic
        adapter = new WorkoutAdapter(getContext(), workoutList, this::showWorkoutDetails);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void initializeWorkouts() {
        workoutList = new ArrayList<>();

        workoutList.add(new Workout(
                "Rutina de Empuje (Push)",
                "Enfoque en pecho, hombros y tríceps.",
                "60 min",
                "Medio",
                "1. Press de Banca: 4x10\n2. Press Militar: 3x12\n3. Fondos en Paralelas: 3xFall\n4. Aperturas con Mancuernas: 3x15\n5. Extensión de Tríceps: 4x12",
                R.drawable.ic_fitness,
                "#FF6B6B" // Rojo
        ));

        workoutList.add(new Workout(
                "Rutina de Tracción (Pull)",
                "Enfoque en espalda, bíceps y trapecios.",
                "55 min",
                "Difícil",
                "1. Dominadas: 4x8\n2. Remo con Barra: 4x10\n3. Jalón al Pecho: 3x12\n4. Curl de Bíceps con Barra: 4x10\n5. Face Pulls: 3x15",
                R.drawable.ic_fitness,
                "#4ECDC4" // Turquesa
        ));

        workoutList.add(new Workout(
                "Día de Pierna (Leg Day)",
                "Construye piernas fuertes y resistentes.",
                "70 min",
                "Difícil",
                "1. Sentadilla Libre: 4x8\n2. Prensa de Piernas: 4x12\n3. Peso Muerto Rumano: 3x10\n4. Extensiones de Cuádriceps: 3x15\n5. Elevación de Talones: 4x20",
                R.drawable.ic_fitness,
                "#FFD166" // Amarillo
        ));

        workoutList.add(new Workout(
                "Cardio HIIT",
                "Quema calorías rápido en poco tiempo.",
                "25 min",
                "Intenso",
                "1. Sprint 30s / Descanso 30s (x10)\n2. Burpees: 3x15\n3. Saltos al Cajón: 3x12\n4. Mountain Climbers: 3x40s",
                R.drawable.ic_fitness,
                "#EF476F" // Rosa
        ));

        workoutList.add(new Workout(
                "Estiramientos & Yoga",
                "Recuperación activa y flexibilidad.",
                "20 min",
                "Fácil",
                "1. Saludo al Sol (x5)\n2. Perro Boca Abajo: 2 min\n3. Estiramiento de Isquios: 30s/lado\n4. Postura del Niño: 3 min",
                R.drawable.ic_fitness,
                "#118AB2" // Azul
        ));
    }

    private void showWorkoutDetails(Workout workout) {
        if (getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_workout, null);

        // Referencias del BottomSheet
        TextView title = sheetView.findViewById(R.id.sheet_title);
        TextView exercises = sheetView.findViewById(R.id.sheet_exercises);
        TextView timerText = sheetView.findViewById(R.id.timer_text);
        MaterialButton btnTimer = sheetView.findViewById(R.id.btn_start_timer);
        MaterialButton btnClose = sheetView.findViewById(R.id.btn_close_sheet);

        // Llenar datos
        title.setText(workout.getTitle());
        exercises.setText(workout.getExercisesList());

        // --- LÓGICA DEL CRONÓMETRO DE DESCANSO ---
        btnTimer.setOnClickListener(v -> {
            if (isTimerRunning) {
                // Si está corriendo, lo cancelamos
                if (restTimer != null) restTimer.cancel();
                isTimerRunning = false;
                btnTimer.setText("Iniciar Descanso (60s)");
                timerText.setText("00:60");
                btnTimer.setBackgroundColor(getResources().getColor(R.color.teal_200, null)); // O color por defecto
            } else {
                // Iniciar cuenta atrás de 60 segundos
                isTimerRunning = true;
                btnTimer.setText("Cancelar");
                btnTimer.setBackgroundColor(0xFFE57373); // Rojo claro para cancelar

                restTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        timerText.setText(String.format(Locale.getDefault(), "00:%02d", seconds));
                    }

                    @Override
                    public void onFinish() {
                        timerText.setText("¡A DARLE! 💪");
                        isTimerRunning = false;
                        btnTimer.setText("Iniciar Descanso (60s)");
                        btnTimer.setBackgroundColor(0xFF4ECDC4); // Volver a Turquesa

                        // Vibración o sonido opcional
                        Toast.makeText(getContext(), "¡Descanso terminado!", Toast.LENGTH_SHORT).show();
                    }
                }.start();
            }
        });

        // Limpiar timer al cerrar
        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (restTimer != null) {
                restTimer.cancel();
                isTimerRunning = false;
            }
        });

        btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}