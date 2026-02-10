package com.example.proyectofitrition;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargar el fragmento inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_workouts) {
                    selectedFragment = new WorkoutsFragment();
                } else if (item.getItemId() == R.id.nav_nutrition) {
                    selectedFragment = new NutritionFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            };
}