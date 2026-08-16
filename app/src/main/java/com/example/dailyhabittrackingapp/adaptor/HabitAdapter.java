package com.example.dailyhabittrackingapp.adaptor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.google.android.material.imageview.ShapeableImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyhabittrackingapp.EditHabitActivity;
import com.example.dailyhabittrackingapp.R;
import com.example.dailyhabittrackingapp.bean.HabitBean;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.MyViewHolder> {

    List<HabitBean> arr;
    List<HabitBean> fullList;
    Context context;
    OnHabitDeleteListener deleteListener;
    OnHabitFinishListener finishListener;
    String filterText = "";

    public interface OnHabitDeleteListener {
        void onDelete(int habitId, int position);
    }

    public interface OnHabitFinishListener {
        void onFinishToggle(int habitId, int position, boolean isFinished);
    }

    public HabitAdapter(List<HabitBean> arr, Context context,
                        OnHabitDeleteListener deleteListener) {
        this(arr, context, deleteListener, null);
    }

    public HabitAdapter(List<HabitBean> arr, Context context,
                        OnHabitDeleteListener deleteListener,
                        OnHabitFinishListener finishListener) {
        this.arr = arr;
        this.fullList = new ArrayList<>(arr);
        this.context = context;
        this.deleteListener = deleteListener;
        this.finishListener = finishListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HabitBean habitBean = arr.get(position);
        holder.itemTitle.setText(habitBean.getTitle());
        holder.itemContent.setText(habitBean.getContent());
        holder.itemDate.setText(habitBean.getDate());
        holder.itemTime.setText(habitBean.getTime());

        // Switch state
        holder.switchFinished.setOnCheckedChangeListener(null);
        holder.switchFinished.setChecked(habitBean.isFinished());
        holder.switchFinished.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && finishListener != null) {
                finishListener.onFinishToggle(arr.get(pos).getId(), pos, isChecked);
            }
        });

        // Streak label
        int streak = habitBean.getStreak();
        if (streak >= 2) {
            holder.itemStreak.setText("\uD83D\uDD25 " + streak + "\u5929");
            holder.itemStreak.setVisibility(View.VISIBLE);
        } else {
            holder.itemStreak.setVisibility(View.GONE);
        }

        // Image
        String imageStr = habitBean.getImageUri();
        if (imageStr != null && !imageStr.isEmpty()) {
            try {
                holder.itemImage.setImageURI(Uri.parse(imageStr));
            } catch (SecurityException e) {
                holder.itemImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            holder.itemImage.setImageResource(R.drawable.default_avatar);
        }

        // Image click → fullscreen
        holder.itemImage.setOnClickListener(v -> showFullScreenImage(habitBean.getImageUri()));

        // Card click → edit
        holder.itemHabit.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditHabitActivity.class);
            intent.putExtra("id", habitBean.getId());
            intent.putExtra("title", habitBean.getTitle());
            intent.putExtra("content", habitBean.getContent());
            intent.putExtra("imageuri", habitBean.getImageUri());
            intent.putExtra("date", habitBean.getDate());
            intent.putExtra("time", habitBean.getTime());
            context.startActivity(intent);
            ((android.app.Activity) context).overridePendingTransition(
                    com.example.dailyhabittrackingapp.R.anim.slide_in_right,
                    com.example.dailyhabittrackingapp.R.anim.slide_out_left);
        });

        // Long click → delete
        holder.itemHabit.setOnLongClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("确认删除这条习惯记录？");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确认", (dialogInterface, i) -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                HabitBean bean = arr.get(pos);
                if (deleteListener != null) {
                    deleteListener.onDelete(bean.getId(), pos);
                }
            });
            dialog.setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
            dialog.create().show();
            return false;
        });
    }

    private void showFullScreenImage(String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) {
            Toast.makeText(context, "\u6CA1\u6709\u56FE\u7247", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_image_fullscreen);

        ImageView fullImage = dialog.findViewById(R.id.img_fullscreen);
        ImageButton btClose = dialog.findViewById(R.id.btn_close);

        try {
            fullImage.setImageURI(Uri.parse(imageUri));
        } catch (Exception e) {
            fullImage.setImageResource(R.drawable.default_avatar);
        }

        btClose.setOnClickListener(v -> dialog.dismiss());
        fullImage.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public void updateList(List<HabitBean> newList) {
        this.fullList = new ArrayList<>(newList);
        this.arr = new ArrayList<>(newList);
        filterByText(filterText);
    }

    public void filterByText(String text) {
        this.filterText = text != null ? text.toLowerCase().trim() : "";
        applyFilters();
    }

    public void filterByStatus(int status) {
        // status: 0=all, 1=unfinished, 2=finished
        applyFiltersWithStatus(status);
    }

    private int currentStatus = 0;

    private void applyFiltersWithStatus(int status) {
        currentStatus = status;
        applyFilters();
    }

    private void applyFilters() {
        List<HabitBean> filtered = new ArrayList<>();
        for (HabitBean bean : fullList) {
            boolean matchText = filterText.isEmpty()
                    || (bean.getTitle() != null && bean.getTitle().toLowerCase().contains(filterText))
                    || (bean.getContent() != null && bean.getContent().toLowerCase().contains(filterText));
            boolean matchStatus = currentStatus == 0
                    || (currentStatus == 1 && !bean.isFinished())
                    || (currentStatus == 2 && bean.isFinished());
            if (matchText && matchStatus) {
                filtered.add(bean);
            }
        }
        arr = new ArrayList<>(filtered);
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemContent, itemDate, itemTime, itemStreak;
        ShapeableImageView itemImage;
        LinearLayout itemHabit;
        SwitchCompat switchFinished;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemContent = itemView.findViewById(R.id.item_content);
            itemDate = itemView.findViewById(R.id.item_date);
            itemTime = itemView.findViewById(R.id.item_time);
            itemImage = itemView.findViewById(R.id.item_image);
            itemHabit = itemView.findViewById(R.id.item_habit);
            itemStreak = itemView.findViewById(R.id.item_streak);
            switchFinished = itemView.findViewById(R.id.switch_finished);
        }
    }
}
