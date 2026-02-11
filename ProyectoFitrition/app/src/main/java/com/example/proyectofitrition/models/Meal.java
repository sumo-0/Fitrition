package com.example.proyectofitrition.models;

import com.google.gson.annotations.SerializedName;

public class Meal {
    @SerializedName("meal_id")
    private String mealId;

    @SerializedName("user_id")
    private String userId;

    private String name;
    private int calories;
    private double proteins;
    private double carbs;
    private double fats;

    @SerializedName("meal_type")
    private String mealType;

    private long timestamp;

    @SerializedName("image_url")
    private String imageUrl;

    public Meal() {}

    public Meal(String mealId, String userId, String name, int calories,
                double proteins, double carbs, double fats, String mealType,
                long timestamp, String imageUrl) {
        this.mealId = mealId;
        this.userId = userId;
        this.name = name;
        this.calories = calories;
        this.proteins = proteins;
        this.carbs = carbs;
        this.fats = fats;
        this.mealType = mealType;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    // Getters y Setters
    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProteins() { return proteins; }
    public void setProteins(double proteins) { this.proteins = proteins; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFats() { return fats; }
    public void setFats(double fats) { this.fats = fats; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}