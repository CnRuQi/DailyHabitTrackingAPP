package com.example.dailyhabittrackingapp;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.dailyhabittrackingapp.db.HabitDbHelper;

public class EditHabitActivity extends BaseHabitActivity {
    private Button btUpdate;
    private int habitId;
    private String oldImageUri;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_edit_habit;
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        btUpdate = findViewById(R.id.bt_update);

        habitId = getIntent().getIntExtra("id", 0);
        String initTitle = getIntent().getStringExtra("title");
        String initContent = getIntent().getStringExtra("content");
        oldImageUri = getIntent().getStringExtra("imageuri");

        edTitle.setText(initTitle);
        edContent.setText(initContent);
        if (oldImageUri != null && !oldImageUri.isEmpty()) {
            ivImage.setImageURI(Uri.parse(oldImageUri));
        }

        btUpdate.setOnClickListener(view -> {
            String editTitle = edTitle.getText().toString();
            String editDesc = edContent.getText().toString();
            String imageStr = imageUri != null ? imageUri.toString() : (oldImageUri != null ? oldImageUri : "");

            // 保留原始日期时间，不覆盖
            String date = getIntent().getStringExtra("date");
            String timeVal = getIntent().getStringExtra("time");

            if (editTitle.isEmpty()) {
                Toast.makeText(EditHabitActivity.this, R.string.toast_enter_habit_title, Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("title", editTitle);
            values.put("content", editDesc);
            values.put("imageuri", imageStr);
            values.put("date", date);
            values.put("time", timeVal);
            int rows = db.update(HabitDbHelper.TABLE_HABIT, values, "id=?",
                    new String[]{String.valueOf(habitId)});

            if (rows > 0) {
                Toast.makeText(EditHabitActivity.this, R.string.toast_update_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditHabitActivity.this, R.string.toast_update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}