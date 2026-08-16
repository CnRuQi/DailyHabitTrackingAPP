package com.example.dailyhabittrackingapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayHolder> {

    private final int year;
    private final int month;
    private final int firstDayOfWeek; // 0=Mon, 0-6
    private final int daysInMonth;
    private final Set<String> checkinDates; // "yyyy-MM-dd"
    private final String todayStr;

    public CalendarAdapter(int year, int month, Set<String> checkinDates) {
        this.year = year;
        this.month = month;
        this.checkinDates = checkinDates;
        this.todayStr = LocalDate.now().toString();

        LocalDate first = LocalDate.of(year, month, 1);
        this.daysInMonth = first.lengthOfMonth();
        // DayOfWeek: MONDAY=1 ... SUNDAY=7. Convert to 0=Mon, 6=Sun
        int dow = first.getDayOfWeek().getValue() - 1;
        this.firstDayOfWeek = dow;
    }

    @Override
    public int getItemCount() {
        return firstDayOfWeek + daysInMonth;
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        int dayNum = position - firstDayOfWeek + 1;
        if (dayNum < 1 || dayNum > daysInMonth) {
            holder.tvDay.setText("");
            holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_cell);
            holder.tvDay.setTextColor(holder.tvDay.getContext().getResources().getColor(R.color.ios_fill_tertiary));
            return;
        }
        holder.tvDay.setText(String.valueOf(dayNum));
        String dateStr = String.format("%04d-%02d-%02d", year, month, dayNum);

        if (dateStr.equals(todayStr)) {
            holder.tvDay.setTextColor(Color.WHITE);
            holder.tvDay.setBackgroundResource(R.drawable.bg_btn_primary);
        } else if (checkinDates.contains(dateStr)) {
            holder.tvDay.setBackgroundColor(0xFF34C759);
            holder.tvDay.setTextColor(Color.WHITE);
        } else {
            holder.tvDay.setTextColor(holder.tvDay.getContext().getResources().getColor(R.color.ios_secondary_label));
            holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_cell);
        }
    }

    static class DayHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        DayHolder(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tv_day);
        }
    }
}
