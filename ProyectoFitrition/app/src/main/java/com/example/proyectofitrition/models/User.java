package com.example.proyectofitrition.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private String userId;

    private String email;
    private double weight;
    private double height;
    private int age;
    private double bmi;

    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    // Constructor SIN name
    public User(String userId, String email, double weight, double height, int age) {
        this.userId = userId;
        this.email = email;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.bmi = calculateBMI(weight, height);
    }

    private double calculateBMI(double weight, double height) {
        if (height > 0) {
            return Math.round((weight / Math.pow(height / 100, 2)) * 10.0) / 10.0;
        }
        return 0;
    }

    public void updateWeight(double newWeight) {
        this.weight = newWeight;
        this.bmi = calculateBMI(newWeight, this.height);
    }

    // Getters y Setters (SIN name)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) {
        this.weight = weight;
        this.bmi = calculateBMI(weight, height);
    }

    public double getHeight() { return height; }
    public void setHeight(double height) {
        this.height = height;
        this.bmi = calculateBMI(weight, height);
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}