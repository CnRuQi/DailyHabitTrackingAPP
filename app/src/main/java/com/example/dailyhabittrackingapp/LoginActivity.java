package com.example.dailyhabittrackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyhabittrackingapp.db.HabitDbHelper;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "HabitApp";
    EditText edUsername, edPassword;
    CheckBox cbRemember;
    Button btLogin, btRegister;
    HabitDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edUsername = findViewById(R.id.ed_username);
        edPassword = findViewById(R.id.ed_password);
        cbRemember = findViewById(R.id.cb_remember);
        btLogin = findViewById(R.id.bt_login);
        btRegister = findViewById(R.id.bt_register);

        dbHelper = new HabitDbHelper(this);
        db = dbHelper.getReadableDatabase();

        getSave();

        btLogin.setOnClickListener(view -> {
            String username = edUsername.getText().toString();
            String password = edPassword.getText().toString();

            if (username.isEmpty()) {
                Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.query(HabitDbHelper.TABLE_USER, null,
                    "username=? AND password=?", new String[]{username, password},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int userId = idIndex >= 0 ? cursor.getInt(idIndex) : -1;
                Log.d(TAG, "Login success: " + username + " (id=" + userId + ")");
                SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                editor.putInt("user_id", userId);
                editor.putString("username", username);
                if (cbRemember.isChecked()) {
                    editor.putString("password", password);
                    editor.putBoolean("remember", true);
                } else {
                    editor.remove("password");
                    editor.putBoolean("remember", false);
                }
                editor.apply();
                    Intent intent = new Intent(LoginActivity.this, HabitListActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
            } else {
                Log.d(TAG, "Login failed: " + username);
                Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        });

        btRegister.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void getSave() {
        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        boolean remember = sp.getBoolean("remember", false);
        if (remember) {
            edUsername.setText(sp.getString("username", ""));
            edPassword.setText(sp.getString("password", ""));
            cbRemember.setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
