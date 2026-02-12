package com.example.proyectofitrition;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Workout workout);
    }

    public WorkoutAdapter(Context context, List<Workout> workoutList, OnItemClickListener listener) {
        this.context = context;
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);

        holder.title.setText(workout.getTitle());
        holder.description.setText(workout.getDescription());
        holder.duration.setText("⏱ " + workout.getDuration());
        holder.difficulty.setText("🔥 " + workout.getDifficulty());
        holder.icon.setImageResource(workout.getIconResId());

        // Colorear el fondo del icono dinámicamente
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(Color.parseColor(workout.getColorHex()));
        holder.iconBackground.setBackground(background);

        holder.card.setOnClickListener(v -> listener.onItemClick(workout));
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, duration, difficulty;
        ImageView icon;
        View iconBackground;
        CardView card;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_workout_title);
            description = itemView.findViewById(R.id.text_workout_desc);
            duration = itemView.findViewById(R.id.text_duration);
            difficulty = itemView.findViewById(R.id.text_difficulty);
            icon = itemView.findViewById(R.id.img_workout_icon);
            iconBackground = itemView.findViewById(R.id.icon_background);
            card = itemView.findViewById(R.id.card_workout_item);
        }
    }
}