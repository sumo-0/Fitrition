package com.example.proyectofitrition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
    private OnMealDeleteListener deleteListener;

    // Interface para el callback de eliminación
    public interface OnMealDeleteListener {
        void onDelete(Meal meal, int position);
    }

    public MealAdapter(List<Meal> meals, Context context) {
        this.meals = meals;
        this.context = context;
    }

    public void setOnMealDeleteListener(OnMealDeleteListener listener) {
        this.deleteListener = listener;
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

        // CARGAR IMAGEN
        if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
            try {
                String base64Image = meal.getImageUrl();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }

                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                holder.ivMeal.setImageBitmap(bitmap);
                holder.ivMeal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                holder.ivMeal.setImageResource(R.drawable.ic_nutrition);
            }
        } else {
            holder.ivMeal.setImageResource(R.drawable.ic_nutrition);
        }

        // BOTÓN ELIMINAR
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(meal, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMeal, btnDelete;
        TextView tvName, tvCalories, tvMacros, tvDate;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal_image);
            tvName = itemView.findViewById(R.id.tv_meal_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvMacros = itemView.findViewById(R.id.tv_macros);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_meal);
        }
    }
}