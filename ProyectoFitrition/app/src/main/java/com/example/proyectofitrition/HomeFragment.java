package com.example.proyectofitrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Buscar las tarjetas por su ID (definidos en fragment_home.xml)
        CardView cardWorkout = view.findViewById(R.id.card_quick_workout);
        CardView cardNutrition = view.findViewById(R.id.card_quick_nutrition);

        // 2. Configurar clic para "Entrenar"
        cardWorkout.setOnClickListener(v -> {
            // Navegar al fragmento de Workouts
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new WorkoutsFragment())
                    .addToBackStack(null) // Permite volver atrás con el botón de retroceso
                    .commit();

            // Actualizar el menú inferior para resaltar "Entrenamientos"
            actualizarBottomNav(R.id.nav_workouts);
        });

        // 3. Configurar clic para "Nutrición"
        cardNutrition.setOnClickListener(v -> {
            // Navegar directamente al fragmento de Nutrición (cámara/análisis)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NutritionFragment())
                    .addToBackStack(null)
                    .commit();

            // Actualizar el menú inferior para resaltar "Nutrición"
            actualizarBottomNav(R.id.nav_nutrition);
        });

        return view;
    }

    // Método auxiliar para cambiar el icono seleccionado en el menú inferior
    private void actualizarBottomNav(int itemId) {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(itemId);
            }
        }
    }
}