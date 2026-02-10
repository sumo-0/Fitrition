package com.example.proyectofitrition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color; // Importante para cambiar el color del texto
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.proyectofitrition.databinding.FragmentNutritionBinding;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NutritionFragment extends Fragment {

    private FragmentNutritionBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Tu URL de producción
    private static final String N8N_WEBHOOK_URL = "https://markits.app.n8n.cloud/webhook/analizar-comida";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutritionBinding.inflate(inflater, container, false);
        binding.btnCamera.setOnClickListener(v -> verificarPermisosYAbrirCamara());
        return binding.getRoot();
    }

    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(getContext(), "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No se pudo abrir la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1 && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                binding.imageViewFood.setImageBitmap(imageBitmap);

                // Empezamos la carga
                toggleLoading(true);

                enviarFotoAN8n(imageBitmap);
            }
        }
    }

    // --- CONTROL DE ANIMACIÓN ---
    private void toggleLoading(boolean isLoading) {
        if (binding == null) return;

        if (isLoading) {
            // MOSTRAR animación, OCULTAR todo lo demás
            binding.animationView.setVisibility(View.VISIBLE);
            binding.animationView.playAnimation();
            binding.resultsGrid.setVisibility(View.GONE); // Ocultar resultados viejos

            binding.textViewStatus.setText("Consultando a la IA... ⏳");
            binding.textViewStatus.setTextColor(Color.GRAY); // Color normal
            binding.btnCamera.setEnabled(false);
        } else {
            // OCULTAR animación
            binding.animationView.pauseAnimation();
            binding.animationView.setVisibility(View.GONE);
            binding.btnCamera.setEnabled(true);

            // NOTA: No mostramos el grid aquí automáticamente.
            // Dejamos que onResponse decida si mostrarlo (si es comida) u ocultarlo (si no lo es).
        }
    }

    private void enviarFotoAN8n(Bitmap bitmap) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "comida.jpg",
                        RequestBody.create(byteArray, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(N8N_WEBHOOK_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toggleLoading(false);
                        if (binding != null) binding.textViewStatus.setText("❌ Error de conexión: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Paramos la animación visualmente
                            toggleLoading(false);

                            if (binding == null) return;

                            try {
                                String jsonLimpio = responseData
                                        .replace("```json", "")
                                        .replace("```", "")
                                        .trim();

                                JSONObject json = new JSONObject(jsonLimpio);

                                // Usamos optInt para obtener números directamente
                                int cal = json.optInt("calories", 0);
                                int pro = json.optInt("protein", 0);
                                int fat = json.optInt("fat", 0);
                                int car = json.optInt("carbs", 0);

                                // --- LÓGICA DE DETECCIÓN DE "NO COMIDA" ---
                                // Si la suma de todo es 0, es que la IA no vio comida
                                if (cal == 0 && pro == 0 && fat == 0 && car == 0) {

                                    // CASO 1: NO ES COMIDA
                                    binding.resultsGrid.setVisibility(View.GONE); // Ocultar tarjetas

                                    binding.textViewStatus.setText("⚠️ No parece ser comida.\nIntenta enfocar mejor o probar otro plato.");
                                    binding.textViewStatus.setTextColor(Color.RED); // Ponemos el texto en rojo para avisar

                                } else {

                                    // CASO 2: SÍ ES COMIDA
                                    binding.resultsGrid.setVisibility(View.VISIBLE); // Mostrar tarjetas

                                    binding.tvCalories.setText(String.valueOf(cal));
                                    binding.tvProtein.setText(String.valueOf(pro));
                                    binding.tvFat.setText(String.valueOf(fat));
                                    binding.tvCarbs.setText(String.valueOf(car));

                                    binding.textViewStatus.setText("✅ Análisis completado");
                                    binding.textViewStatus.setTextColor(Color.parseColor("#999999")); // Resetear color a gris
                                }

                            } catch (Exception e) {
                                binding.textViewStatus.setText("⚠️ Error leyendo datos.");
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            toggleLoading(false);
                            if (binding != null) {
                                binding.textViewStatus.setText("❌Escanee una comida de verdad, porfavor❌");
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}