package com.example.punchclock.ui.detail;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punchclock.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final List<LocalDate> days = new ArrayList<>();
    private final Set<LocalDate> punchedDates = new HashSet<>();
    private OnDateClickListener listener;
    private int projectColor = Color.GRAY;

    public interface OnDateClickListener {
        void onDateClick(LocalDate date, boolean isPunched);
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<LocalDate> days) {
        this.days.clear();
        this.days.addAll(days);
        notifyDataSetChanged();
    }

    public void setPunchedDates(List<Long> dates, int color) {
        this.punchedDates.clear();
        for (Long date : dates) {
            this.punchedDates.add(LocalDate.ofEpochDay(date));
        }
        this.projectColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        
        // Calculate item width based on parent width, but provide a fallback
        int parentWidth = parent.getWidth();
        if (parentWidth > 0) {
             layoutParams.height = parentWidth / 7;
        } else {
             // Fallback: Use display metrics to estimate screen width or just set a fixed height
             // Assuming typical screen width ~360dp - 400dp. 
             // Better fallback: just set a reasonable fixed height like 48dp or 50dp
             layoutParams.height = (int) (parent.getContext().getResources().getDisplayMetrics().density * 50);
        }
        
        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);
        
        if (date == null) {
            holder.textDay.setText("");
            holder.textDay.setBackgroundResource(0);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.textDay.setText(String.valueOf(date.getDayOfMonth()));
            
            boolean isPunched = punchedDates.contains(date);
            if (isPunched) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(projectColor);
                holder.textDay.setBackground(drawable);
                holder.textDay.setTextColor(Color.WHITE);
            } else {
                holder.textDay.setBackgroundResource(R.drawable.day_background); // Transparent
                holder.textDay.setTextColor(Color.BLACK);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDateClick(date, isPunched);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.text_day);
        }
    }
}
