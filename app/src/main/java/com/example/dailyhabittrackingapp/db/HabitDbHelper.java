package com.example.dailyhabittrackingapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class HabitDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "habit.db";
    private static final int DB_VERSION = 4;
    public static final String TABLE_USER = "tb_user";
    public static final String TABLE_HABIT = "tb_habit";
    public static final String TABLE_CHECKIN = "tb_checkin";

    public HabitDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createUser = "CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL)";
        sqLiteDatabase.execSQL(createUser);

        String createHabit = "CREATE TABLE " + TABLE_HABIT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT NOT NULL, " +
                "content TEXT, " +
                "imageuri TEXT, " +
                "date TEXT, " +
                "time TEXT)";
        sqLiteDatabase.execSQL(createHabit);

        String createCheckin = "CREATE TABLE " + TABLE_CHECKIN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "habit_id INTEGER, " +
                "checkin_date TEXT)";
        sqLiteDatabase.execSQL(createCheckin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_HABIT +
                    " ADD COLUMN is_finished INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHECKIN + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "habit_id INTEGER, " +
                    "checkin_date TEXT)");
        }
        if (oldVersion < 4) {
            // Add user_id to tb_habit
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_HABIT +
                        " ADD COLUMN user_id INTEGER");
            } catch (Exception ignored) {}
            // Add user_id to tb_checkin
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_CHECKIN +
                        " ADD COLUMN user_id INTEGER");
            } catch (Exception ignored) {}
        }
    }
}