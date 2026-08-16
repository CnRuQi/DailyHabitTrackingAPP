package com.example.dailyhabittrackingapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.dailyhabittrackingapp.db.HabitDbHelper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseHabitActivity extends AppCompatActivity {
    private static final String TAG = "HabitApp";
    protected EditText edTitle, edContent;
    protected Button btCamera, btPhoto;
    protected ImageView ivImage;
    protected Uri imageUri;
    protected HabitDbHelper dbHelper;
    protected SQLiteDatabase db;

    protected abstract int getLayoutResId();
    protected abstract void onViewCreated(Bundle savedInstanceState);

    // ActivityResult launchers
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        Log.d(TAG, getClass().getSimpleName() + " created");

        edTitle = findViewById(R.id.ed_title);
        edContent = findViewById(R.id.ed_content);
        btCamera = findViewById(R.id.bt_camera);
        btPhoto = findViewById(R.id.bt_photo);
        ivImage = findViewById(R.id.iv_image);

        dbHelper = new HabitDbHelper(this);
        db = dbHelper.getWritableDatabase();

        // Register activity result launchers
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        Log.d(TAG, "Photo taken via camera");
                        ivImage.setImageURI(imageUri);
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d(TAG, "Photo selected from gallery");
                        imageUri = uri;
                        ivImage.setImageURI(uri);
                    }
                });

        setupCameraButton();
        setupPhotoButton();

        onViewCreated(savedInstanceState);
    }

    protected void setupCameraButton() {
        btCamera.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(BaseHabitActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BaseHabitActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                openCamera();
            }
        });
    }

    protected void setupPhotoButton() {
        btPhoto.setOnClickListener(view -> pickImageLauncher.launch("image/*"));
    }

    protected void openCamera() {
        Log.d(TAG, "Opening camera");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String stime = now.format(tsFmt);

        File dir = getExternalFilesDir("Pictures");
        if (dir == null) {
            dir = getFilesDir();
        }
        File camera;
        try {
            camera = File.createTempFile(stime, ".jpg", dir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temp file for camera", e);
            Toast.makeText(this, "无法创建照片文件", Toast.LENGTH_SHORT).show();
            return;
        }

        imageUri = FileProvider.getUriForFile(this,
                "com.example.dailyhabittrackingapp.fileprovider", camera);

        try {
            takePictureLauncher.launch(imageUri);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No camera app found", e);
            Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Log.d(TAG, "Camera permission permanently denied");
                showPermissionDeniedDialog();
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, getClass().getSimpleName() + " destroyed");
        if (db != null) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setMessage("相机权限已被永久拒绝，请在系统设置中手动开启")
                .setPositiveButton("前往设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }
}