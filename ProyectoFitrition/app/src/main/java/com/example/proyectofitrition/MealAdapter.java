package com.example.proyectofitrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofitrition.models.Meal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> meals;
    private Context context;

    public MealAdapter(List<Meal> meals, Context context) {
        this.meals = meals;
        this.context = context;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);

        holder.tvName.setText(meal.getName());
        holder.tvCalories.setText(meal.getCalories() + " kcal");
        holder.tvMacros.setText(String.format(Locale.getDefault(),
                "P: %.0fg | C: %.0fg | G: %.0fg",
                meal.getProteins(), meal.getCarbs(), meal.getFats()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(meal.getTimestamp())));

        // Usar ícono genérico (sin imagen por ahora)
        holder.ivMeal.setImageResource(R.drawable.ic_nutrition);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMeal;
        TextView tvName, tvCalories, tvMacros, tvDate;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvName = itemView.findViewById(R.id.tv_meal_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvMacros = itemView.findViewById(R.id.tv_macros);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}