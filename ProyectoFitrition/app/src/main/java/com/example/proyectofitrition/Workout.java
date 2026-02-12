package com.example.proyectofitrition;

public class Workout {
    private String title;
    private String description;
    private String duration;
    private String difficulty; // "Fácil", "Medio", "Difícil"
    private String exercisesList; // Lista de ejercicios en texto plano para simplificar
    private int iconResId;
    private String colorHex;

    public Workout(String title, String description, String duration, String difficulty, String exercisesList, int iconResId, String colorHex) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.difficulty = difficulty;
        this.exercisesList = exercisesList;
        this.iconResId = iconResId;
        this.colorHex = colorHex;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDuration() { return duration; }
    public String getDifficulty() { return difficulty; }
    public String getExercisesList() { return exercisesList; }
    public int getIconResId() { return iconResId; }
    public String getColorHex() { return colorHex; }
}