package com.example.proyectofitrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class Nutrition1Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition1, container, false);

        // Obtener referencia al botón
        MaterialButton btnAddMeal = view.findViewById(R.id.btn_add_meal);

        // Configurar el click para navegar al nuevo fragmento
        btnAddMeal.setOnClickListener(v -> {

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NutritionFragment())
                    .addToBackStack(null) // Permite volver atrás con el botón back
                    .commit();
        });

        return view;
    }
}