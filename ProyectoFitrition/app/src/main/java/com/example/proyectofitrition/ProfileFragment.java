package com.example.proyectofitrition;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.proyectofitrition.api.SupabaseManager;
import com.example.proyectofitrition.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private TextView tvUserEmail, tvUserWeight, tvUserHeight, tvUserAge, tvUserBMI;
    private CardView cardEditWeight, cardLogout;
    private SupabaseManager supabaseManager;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        supabaseManager = new SupabaseManager(requireContext());

        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserWeight = view.findViewById(R.id.tv_user_weight);
        tvUserHeight = view.findViewById(R.id.tv_user_height);
        tvUserAge = view.findViewById(R.id.tv_user_age);
        tvUserBMI = view.findViewById(R.id.tv_user_bmi);
        cardEditWeight = view.findViewById(R.id.card_edit_weight);
        cardLogout = view.findViewById(R.id.card_logout);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Botón editar peso
        cardEditWeight.setOnClickListener(v -> mostrarDialogoEditarPeso());

        // Botón cerrar sesión
        cardLogout.setOnClickListener(v -> cerrarSesion());

        return view;
    }

    private void cargarDatosUsuario() {
        supabaseManager.getCurrentUser(new SupabaseManager.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentUser = user;
                        actualizarUI();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al cargar datos: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void actualizarUI() {
        if (currentUser == null) return;

        tvUserEmail.setText(currentUser.getEmail());
        tvUserWeight.setText(String.format("%.1f kg", currentUser.getWeight()));
        tvUserHeight.setText(String.format("%.0f cm", currentUser.getHeight()));
        tvUserAge.setText(currentUser.getAge() + " años");
        tvUserBMI.setText(String.format("%.1f", currentUser.getBmi()));

        // Colorear IMC según categoría
        double bmi = currentUser.getBmi();
        if (bmi < 18.5) {
            tvUserBMI.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (bmi < 25) {
            tvUserBMI.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (bmi < 30) {
            tvUserBMI.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvUserBMI.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void mostrarDialogoEditarPeso() {
        if (currentUser == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_weight, null);
        TextInputEditText etNewWeight = dialogView.findViewById(R.id.et_new_weight);

        etNewWeight.setText(String.valueOf(currentUser.getWeight()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Actualizar Peso")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String newWeightStr = etNewWeight.getText().toString().trim();
                    if (!newWeightStr.isEmpty()) {
                        try {
                            double newWeight = Double.parseDouble(newWeightStr);
                            if (newWeight > 0 && newWeight <= 300) {
                                actualizarPeso(newWeight);
                            } else {
                                Toast.makeText(getContext(), "Peso entre 1 y 300 kg",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Peso inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarPeso(double newWeight) {
        if (currentUser == null) return;

        currentUser.updateWeight(newWeight);

        supabaseManager.updateUser(currentUser, new SupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "✅ Peso actualizado: " + newWeight + " kg",
                                Toast.LENGTH_SHORT).show();
                        actualizarUI();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "❌ Error al actualizar: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    supabaseManager.signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}