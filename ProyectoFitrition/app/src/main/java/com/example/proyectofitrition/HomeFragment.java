package com.example.proyectofitrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Referencias
        CardView cardWorkout = view.findViewById(R.id.card_quick_workout);
        CardView cardNutrition = view.findViewById(R.id.card_quick_nutrition);

        // 2. Animaciones de entrada
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        // Retraso para la segunda tarjeta (efecto cascada)
        Animation animDelay = AnimationUtils.loadAnimation(getContext(), R.anim.fade_scale_in);
        animDelay.setStartOffset(150);

        cardWorkout.startAnimation(anim);
        cardNutrition.startAnimation(animDelay);

        // 3. Clics (Navegación)
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

    private void actualizarBottomNav(int itemId) {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(itemId);
            }
        }
    }
}