package com.example.proyectofitrition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.proyectofitrition.databinding.FragmentNutritionBinding;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    private static final int REQUEST_CAMERA_PERMISSION = 100; // Código para identificar el permiso

    // IMPORTANTE: Pon tu URL de n8n aquí
    private static final String N8N_WEBHOOK_URL = "https://tu-usuario.app.n8n.cloud/webhook/analizar-comida";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutritionBinding.inflate(inflater, container, false);

        binding.btnCamera.setOnClickListener(v -> verificarPermisosYAbrirCamara());

        return binding.getRoot();
    }

    // NUEVO MÉTODO: Verifica si tenemos permiso antes de abrir nada
    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no tenemos permiso, lo pedimos
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Si ya lo tenemos, abrimos cámara directo
            abrirCamara();
        }
    }

    // Este método recibe la respuesta del usuario (Si dijo "Sí" o "No")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara(); // ¡Nos dio permiso!
            } else {
                Toast.makeText(getContext(), "Se necesita permiso de cámara para analizar comida", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            // En Android 11+ esto a veces falla si no tienes <queries> en el manifest,
            // así que envolvemos en try-catch para seguridad extra.
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
                binding.textViewResult.setText("Analizando... esto puede tardar unos segundos ⏳");

                enviarFotoAN8n(imageBitmap);
            }
        }
    }

    private void enviarFotoAN8n(Bitmap bitmap) {
        OkHttpClient client = new OkHttpClient();

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
                    getActivity().runOnUiThread(() ->
                            binding.textViewResult.setText("Error de conexión: " + e.getMessage())
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                JSONObject json = new JSONObject(responseData);

                                // Ajusta estos nombres según lo que devuelva tu n8n
                                String calorias = json.optString("calories", "0");
                                String prote = json.optString("protein", "0");
                                String grasas = json.optString("fat", "0");
                                String carbs = json.optString("carbs", "0");

                                String resultado =
                                        "🔥 Calorías: " + calorias + " kcal\n" +
                                                "🥩 Proteínas: " + prote + "g\n" +
                                                "🥑 Grasas: " + grasas + "g\n" +
                                                "🍞 Carbohidratos: " + carbs + "g";

                                binding.textViewResult.setText(resultado);

                            } catch (Exception e) {
                                binding.textViewResult.setText("Error leyendo datos: " + responseData);
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                binding.textViewResult.setText("Error del servidor: " + response.code())
                        );
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