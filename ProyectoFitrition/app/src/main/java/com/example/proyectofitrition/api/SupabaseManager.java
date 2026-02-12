package com.example.proyectofitrition.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.proyectofitrition.models.Meal;
import com.example.proyectofitrition.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseManager {

    // REEMPLAZA CON TUS VALORES DE SUPABASE
    private static final String SUPABASE_URL = "https://ihmlbkcvtugrigpnpwfs.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlobWxia2N2dHVncmlncG5wd2ZzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA4MDIwNTAsImV4cCI6MjA4NjM3ODA1MH0.ZIuPMzgl82pO532ySS-qAm33dGNd3mDa7hIEiKlwaps";
    private static final String AUTH_URL = SUPABASE_URL + "/auth/v1";
    private static final String REST_URL = SUPABASE_URL + "/rest/v1";

    private Context context;
    private OkHttpClient client;
    private Gson gson;
    private SharedPreferences prefs;

    public SupabaseManager(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
    }

    // ==================== AUTENTICACIÓN ====================

    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String error);
    }

    public void signUp(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                android.util.Log.d("SUPABASE", "=== INICIANDO SIGNUP ===");
                android.util.Log.d("SUPABASE", "Email: " + email);
                android.util.Log.d("SUPABASE", "URL: " + AUTH_URL + "/signup");

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(AUTH_URL + "/signup")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                android.util.Log.d("SUPABASE", "Status Code: " + response.code());
                android.util.Log.d("SUPABASE", "Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.has("user")) {
                        callback.onError("No se recibió información del usuario");
                        return;
                    }

                    JSONObject user = jsonResponse.getJSONObject("user");
                    String userId = user.getString("id");

                    if (jsonResponse.has("session") && !jsonResponse.isNull("session")) {
                        // ✅ Caso 1: Session disponible inmediatamente
                        android.util.Log.d("SUPABASE", "✅ Session disponible en signup");
                        JSONObject session = jsonResponse.getJSONObject("session");
                        String accessToken = session.getString("access_token");
                        saveSession(userId, accessToken);
                        callback.onSuccess(userId);
                    } else {
                        // ⚠️ Caso 2: No hay session, hacer login automático
                        android.util.Log.w("SUPABASE", "⚠️ NO hay session en signup - haciendo auto-login...");
                        signIn(email, password, callback);
                        return;
                    }
                } else {
                    try {
                        JSONObject error = new JSONObject(responseBody);
                        String errorMsg = error.optString("message", "");

                        if (errorMsg.isEmpty()) {
                            errorMsg = error.optString("error_description", "");
                        }

                        if (errorMsg.isEmpty()) {
                            errorMsg = error.optString("error", "Error desconocido");
                        }

                        callback.onError(errorMsg);
                    } catch (Exception e) {
                        callback.onError("Error en el servidor: " + responseBody);
                    }
                }

            } catch (Exception e) {
                android.util.Log.e("SUPABASE", "Excepción: " + e.getMessage());
                e.printStackTrace();
                callback.onError("Error de conexión: " + e.getMessage());
            }
        }).start();
    }

    public void signIn(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                android.util.Log.d("SUPABASE", "=== INICIANDO LOGIN ===");
                android.util.Log.d("SUPABASE", "Email: " + email);

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(AUTH_URL + "/token?grant_type=password")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                android.util.Log.d("SUPABASE", "Login Status Code: " + response.code());

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (!jsonResponse.has("user")) {
                        callback.onError("Respuesta inválida del servidor");
                        return;
                    }

                    JSONObject user = jsonResponse.getJSONObject("user");
                    String userId = user.getString("id");
                    String accessToken = jsonResponse.getString("access_token");

                    android.util.Log.d("SUPABASE", "✅ Login exitoso - Token guardado");
                    saveSession(userId, accessToken);
                    callback.onSuccess(userId);
                } else {
                    JSONObject error = new JSONObject(responseBody);
                    String errorMsg = error.optString("error_description", "Credenciales incorrectas");
                    android.util.Log.e("SUPABASE", "❌ Error login: " + errorMsg);
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                android.util.Log.e("SUPABASE", "❌ Excepción login: " + e.getMessage());
                callback.onError("Error de conexión: " + e.getMessage());
            }
        }).start();
    }

    public void signOut() {
        prefs.edit().clear().apply();
    }

    public boolean isUserLoggedIn() {
        return prefs.contains("user_id") && prefs.contains("access_token");
    }

    public String getCurrentUserId() {
        return prefs.getString("user_id", null);
    }

    public String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    private void saveSession(String userId, String accessToken) {
        android.util.Log.d("SUPABASE", "💾 Guardando sesión - UserId: " + userId);
        prefs.edit()
                .putString("user_id", userId)
                .putString("access_token", accessToken)
                .apply();
    }

    // ==================== DATABASE ====================

    public interface DatabaseCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface MealsCallback {
        void onSuccess(List<Meal> meals);
        void onError(String error);
    }

    // Crear usuario
    public void createUser(User user, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                android.util.Log.d("SUPABASE_DB", "Creando usuario en DB...");
                android.util.Log.d("SUPABASE_DB", "UserId: " + user.getUserId());
                android.util.Log.d("SUPABASE_DB", "Email: " + user.getEmail());

                String json = gson.toJson(user);
                android.util.Log.d("SUPABASE_DB", "JSON: " + json);

                RequestBody body = RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                );

                Request.Builder requestBuilder = new Request.Builder()
                        .url(REST_URL + "/users")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal");

                String token = getAccessToken();
                if (token != null && !token.equals("pending_confirmation") && token.startsWith("eyJ")) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                    android.util.Log.d("SUPABASE_DB", "✅ Token válido añadido");
                } else {
                    android.util.Log.w("SUPABASE_DB", "⚠️ Sin token válido, usando solo apikey");
                }

                Request request = requestBuilder.post(body).build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                android.util.Log.d("SUPABASE_DB", "Status Code: " + response.code());
                android.util.Log.d("SUPABASE_DB", "Response: " + responseBody);

                if (response.isSuccessful() || response.code() == 201) {
                    android.util.Log.d("SUPABASE_DB", "✅ Usuario creado exitosamente");
                    callback.onSuccess();
                } else {
                    android.util.Log.e("SUPABASE_DB", "❌ Error: " + responseBody);
                    callback.onError(responseBody);
                }
            } catch (Exception e) {
                android.util.Log.e("SUPABASE_DB", "❌ Excepción: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Obtener usuario actual
    public void getCurrentUser(UserCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();

                Request request = new Request.Builder()
                        .url(REST_URL + "/users?user_id=eq." + userId)
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    if (jsonArray.length() > 0) {
                        User user = gson.fromJson(jsonArray.getJSONObject(0).toString(), User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Usuario no encontrado");
                    }
                } else {
                    callback.onError(responseBody);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Actualizar usuario
    public void updateUser(User user, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                String json = gson.toJson(user);

                RequestBody body = RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(REST_URL + "/users?user_id=eq." + user.getUserId())
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(response.body().string());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Crear comida
    public void createMeal(Meal meal, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                android.util.Log.d("SUPABASE_MEALS", "=== CREANDO COMIDA ===");
                android.util.Log.d("SUPABASE_MEALS", "Meal: " + meal.getName());

                String json = gson.toJson(meal);
                android.util.Log.d("SUPABASE_MEALS", "JSON: " + json);

                RequestBody body = RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(REST_URL + "/meals")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                android.util.Log.d("SUPABASE_MEALS", "Status: " + response.code());
                android.util.Log.d("SUPABASE_MEALS", "Response: " + responseBody);

                if (response.isSuccessful() || response.code() == 201) {
                    android.util.Log.d("SUPABASE_MEALS", "✅ Comida creada");
                    callback.onSuccess();
                } else {
                    android.util.Log.e("SUPABASE_MEALS", "❌ Error: " + responseBody);
                    callback.onError(responseBody);
                }
            } catch (Exception e) {
                android.util.Log.e("SUPABASE_MEALS", "❌ Excepción: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Obtener comidas del usuario
    public void getUserMeals(MealsCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                android.util.Log.d("SUPABASE_MEALS", "=== OBTENIENDO COMIDAS ===");
                android.util.Log.d("SUPABASE_MEALS", "UserId: " + userId);

                Request request = new Request.Builder()
                        .url(REST_URL + "/meals?user_id=eq." + userId + "&order=timestamp.desc")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                android.util.Log.d("SUPABASE_MEALS", "Status: " + response.code());
                android.util.Log.d("SUPABASE_MEALS", "Response: " + responseBody);

                if (response.isSuccessful()) {
                    Type listType = new TypeToken<List<Meal>>(){}.getType();
                    List<Meal> meals = gson.fromJson(responseBody, listType);
                    android.util.Log.d("SUPABASE_MEALS", "✅ Comidas parseadas: " + (meals != null ? meals.size() : 0));
                    callback.onSuccess(meals != null ? meals : new ArrayList<>());
                } else {
                    android.util.Log.e("SUPABASE_MEALS", "❌ Error: " + responseBody);
                    callback.onError(responseBody);
                }
            } catch (Exception e) {
                android.util.Log.e("SUPABASE_MEALS", "❌ Excepción: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }
}