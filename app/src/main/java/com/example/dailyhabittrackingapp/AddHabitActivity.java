package com.example.dailyhabittrackingapp;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.dailyhabittrackingapp.db.HabitDbHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddHabitActivity extends BaseHabitActivity {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_habit;
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        int currentUserId = getSharedPreferences("login", MODE_PRIVATE).getInt("user_id", -1);
        Button btSave = findViewById(R.id.bt_save);

        btSave.setOnClickListener(view -> {
            String title = edTitle.getText().toString();
            String content = edContent.getText().toString();
            String imageStr = imageUri != null ? imageUri.toString() : "";

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            String date = now.format(dateFmt);
            String currentTime = now.format(timeFmt);

            if (title.isEmpty()) {
                Toast.makeText(AddHabitActivity.this, R.string.toast_enter_habit_title, Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("user_id", currentUserId);
            values.put("title", title);
            values.put("content", content);
            values.put("imageuri", imageStr);
            values.put("date", date);
            values.put("time", currentTime);
            long result = db.insert(HabitDbHelper.TABLE_HABIT, null, values);

            if (result != -1) {
                Toast.makeText(AddHabitActivity.this, R.string.toast_add_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddHabitActivity.this, R.string.toast_add_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}