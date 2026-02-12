package com.example.proyectofitrition;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.example.proyectofitrition.api.SupabaseManager;
import com.example.proyectofitrition.databinding.FragmentNutritionBinding;
import com.example.proyectofitrition.models.Meal;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
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

    private Bitmap fotoActual;
    private static final String URL_ANALISIS = "https://markits.app.n8n.cloud/webhook-test/analizar-comida";
    private static final String URL_RECETA = "https://markits.app.n8n.cloud/webhook-test/receta-chef";

    private SupabaseManager supabaseManager;

    // Datos actuales de la comida
    private int currentCalories = 0;
    private int currentProtein = 0;
    private int currentFat = 0;
    private int currentCarbs = 0;
    private String currentFoodName = "Comida detectada";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutritionBinding.inflate(inflater, container, false);

        supabaseManager = new SupabaseManager(requireContext());

        binding.btnCamera.setOnClickListener(v -> verificarPermisosYAbrirCamara());
        binding.btnChef.setOnClickListener(v -> pedirRecetaChef());

        return binding.getRoot();
    }

    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            Toast.makeText(getContext(), "Permiso necesario", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1 && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                fotoActual = (Bitmap) extras.get("data");
                binding.imageViewFood.setImageBitmap(fotoActual);
                toggleLoading(true);
                enviarFotoAN8n(fotoActual, URL_ANALISIS, true);
            }
        }
    }

    private void toggleLoading(boolean isLoading) {
        if (binding == null) return;

        if (isLoading) {
            binding.animationView.setVisibility(View.VISIBLE);
            binding.animationView.playAnimation();
            binding.resultsGrid.setVisibility(View.GONE);
            binding.btnChef.setVisibility(View.GONE);
            binding.textViewStatus.setText("Consultando a la IA... ⏳");
            binding.textViewStatus.setTextColor(Color.GRAY);
            binding.btnCamera.setEnabled(false);
        } else {
            binding.animationView.pauseAnimation();
            binding.animationView.setVisibility(View.GONE);
            binding.btnCamera.setEnabled(true);
        }
    }

    private void pedirRecetaChef() {
        if (fotoActual == null) return;

        binding.textViewStatus.setText("👨‍🍳 El Chef está escribiendo tu receta...");
        binding.btnChef.setEnabled(false);
        binding.btnChef.setText("Cocinando... 🔥");

        enviarFotoAN8n(fotoActual, URL_RECETA, false);
    }

    private void enviarFotoAN8n(Bitmap bitmap, String url, boolean esAnalisis) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "comida.jpg", RequestBody.create(stream.toByteArray(), MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toggleLoading(false);
                        binding.btnChef.setEnabled(true);
                        binding.btnChef.setText("👨‍🍳 ¿Cómo lo cocino?");
                        binding.textViewStatus.setText("❌ Error de conexión");
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding == null) return;

                            if (esAnalisis) {
                                toggleLoading(false);
                                procesarAnalisis(responseData);
                            } else {
                                binding.btnChef.setEnabled(true);
                                binding.btnChef.setText("👨‍🍳 ¿Cómo lo cocino?");
                                binding.textViewStatus.setText("✅ Receta lista");
                                mostrarRecetaEnDialogo(responseData);
                            }
                        });
                    }
                }
            }
        });
    }

    private void procesarAnalisis(String jsonRaw) {
        try {
            String jsonLimpio = jsonRaw.replace("```json", "").replace("```", "").trim();
            JSONObject json = new JSONObject(jsonLimpio);

            currentCalories = json.optInt("calories", 0);
            currentProtein = json.optInt("protein", 0);
            currentFat = json.optInt("fat", 0);
            currentCarbs = json.optInt("carbs", 0);
            currentFoodName = json.optString("name", "Comida detectada");

            if (currentCalories == 0 && currentProtein == 0 && currentFat == 0 && currentCarbs == 0) {
                binding.resultsGrid.setVisibility(View.GONE);
                binding.btnChef.setVisibility(View.GONE);
                binding.textViewStatus.setText("⚠️ No parece ser comida.");
                binding.textViewStatus.setTextColor(Color.RED);
            } else {
                binding.resultsGrid.setVisibility(View.VISIBLE);
                binding.btnChef.setVisibility(View.VISIBLE);

                binding.tvCalories.setText(String.valueOf(currentCalories));
                binding.tvProtein.setText(String.valueOf(currentProtein));
                binding.tvFat.setText(String.valueOf(currentFat));
                binding.tvCarbs.setText(String.valueOf(currentCarbs));

                binding.textViewStatus.setText("✅ Análisis completado");
                binding.textViewStatus.setTextColor(Color.parseColor("#999999"));

                // Mostrar diálogo para guardar
                mostrarDialogoGuardar();
            }
        } catch (Exception e) {
            binding.textViewStatus.setText("⚠️ Error leyendo datos.");
        }
    }

    private void mostrarDialogoGuardar() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Guardar Comida")
                .setMessage("¿Deseas guardar esta comida en tu registro diario?")
                .setPositiveButton("Guardar", (dialog, which) -> guardarComidaEnSupabase())
                .setNegativeButton("Ahora no", null)
                .show();
    }

    private void guardarComidaEnSupabase() {
        if (!supabaseManager.isUserLoggedIn()) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCalories == 0 && currentProtein == 0 && currentFat == 0 && currentCarbs == 0) {
            Toast.makeText(getContext(), "No hay datos para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.textViewStatus.setText("Guardando... 💾");

        String mealId = UUID.randomUUID().toString();
        String userId = supabaseManager.getCurrentUserId();

        // CONVERTIR BITMAP A BASE64
        String imageBase64 = null;
        if (fotoActual != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fotoActual.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            imageBase64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
        }

        Meal meal = new Meal(
                mealId,
                userId,
                currentFoodName,
                currentCalories,
                currentProtein,
                currentCarbs,
                currentFat,
                "Escaneada",
                System.currentTimeMillis(),
                imageBase64  // ✅ AQUÍ LA IMAGEN EN BASE64
        );

        supabaseManager.createMeal(meal, new SupabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "✅ Comida guardada correctamente",
                                Toast.LENGTH_SHORT).show();
                        binding.textViewStatus.setText("✅ Guardado exitoso");
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "❌ Error al guardar: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void mostrarRecetaEnDialogo(String receta) {
        new AlertDialog.Builder(getContext())
                .setTitle("👨‍🍳 Receta del Chef")
                .setMessage(receta)
                .setPositiveButton("¡Gracias!", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}