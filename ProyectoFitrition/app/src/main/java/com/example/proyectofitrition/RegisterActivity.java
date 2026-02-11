package com.example.proyectofitrition;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofitrition.api.SupabaseManager;
import com.example.proyectofitrition.models.User;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private EditText etWeight, etHeight, etAge;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private SupabaseManager supabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        supabaseManager = new SupabaseManager(this);

        // Referencias (SIN etName)
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        etAge = findViewById(R.id.et_age);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        // Validaciones de email
        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }

        // Validaciones de contraseña
        if (password.isEmpty()) {
            etPassword.setError("Ingresa una contraseña");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        // Validaciones de datos personales
        if (weightStr.isEmpty()) {
            etWeight.setError("Ingresa tu peso");
            etWeight.requestFocus();
            return;
        }

        if (heightStr.isEmpty()) {
            etHeight.setError("Ingresa tu altura");
            etHeight.requestFocus();
            return;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Ingresa tu edad");
            etAge.requestFocus();
            return;
        }

        double weight;
        double height;
        int age;

        try {
            weight = Double.parseDouble(weightStr);
            height = Double.parseDouble(heightStr);
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores numéricos inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validaciones de rangos
        if (weight <= 0 || weight > 300) {
            etWeight.setError("Peso entre 1 y 300 kg");
            etWeight.requestFocus();
            return;
        }

        if (height <= 0 || height > 250) {
            etHeight.setError("Altura entre 1 y 250 cm");
            etHeight.requestFocus();
            return;
        }

        if (age <= 0 || age > 120) {
            etAge.setError("Edad entre 1 y 120 años");
            etAge.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        final double finalWeight = weight;
        final double finalHeight = height;
        final int finalAge = age;

        // Log de inicio
        android.util.Log.d("REGISTER", "=== INICIANDO REGISTRO ===");
        android.util.Log.d("REGISTER", "Email: " + email);
        android.util.Log.d("REGISTER", "Password length: " + password.length());
        android.util.Log.d("REGISTER", "Weight: " + finalWeight);
        android.util.Log.d("REGISTER", "Height: " + finalHeight);
        android.util.Log.d("REGISTER", "Age: " + finalAge);

        // Registrar en Supabase Auth
        supabaseManager.signUp(email, password, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                android.util.Log.d("REGISTER", "✅ SignUp exitoso, userId: " + userId);

                // Crear usuario en la base de datos (SIN name, solo con email)
                User user = new User(userId, email, finalWeight, finalHeight, finalAge);

                android.util.Log.d("REGISTER", "Intentando guardar en base de datos...");

                supabaseManager.createUser(user, new SupabaseManager.DatabaseCallback() {
                    @Override
                    public void onSuccess() {
                        android.util.Log.d("REGISTER", "✅ Usuario guardado en DB correctamente");
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this,
                                    "¡Registro exitoso! 🎉",
                                    Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("REGISTER", "❌ Error al guardar en DB: " + error);
                        runOnUiThread(() -> {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTRARSE");
                            Toast.makeText(RegisterActivity.this,
                                    "Error al guardar datos: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("REGISTER", "❌ Error en signUp: " + error);
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("REGISTRARSE");

                    String errorMessage;
                    if (error.contains("already registered") || error.contains("already been registered")) {
                        errorMessage = "Este email ya está registrado";
                    } else if (error.contains("Password")) {
                        errorMessage = "La contraseña debe ser más fuerte (mín. 6 caracteres)";
                    } else if (error.contains("invalid")) {
                        errorMessage = "Email o contraseña inválidos";
                    } else if (error.contains("pending_confirmation")) {
                        errorMessage = "Debes confirmar tu email primero";
                    } else {
                        errorMessage = "Error en registro: " + error;
                    }

                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}