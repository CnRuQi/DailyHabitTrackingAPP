package com.example.dailyhabittrackingapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dailyhabittrackingapp.db.HabitDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstrumentedTest {
    private HabitDbHelper dbHelper;
    private SQLiteDatabase db;

    @Before
    public void setUp() {
        dbHelper = new HabitDbHelper(getInstrumentation().getTargetContext());
        db = dbHelper.getWritableDatabase();
        // Clear before each test
        db.delete(HabitDbHelper.TABLE_HABIT, null, null);
        db.delete(HabitDbHelper.TABLE_USER, null, null);
    }

    @After
    public void tearDown() {
        if (db != null) db.close();
        if (dbHelper != null) dbHelper.close();
    }

    @Test
    public void testUserInsert() {
        ContentValues values = new ContentValues();
        values.put("username", "testuser");
        values.put("password", "123456");
        long result = db.insert(HabitDbHelper.TABLE_USER, null, values);
        assertNotEquals(-1, result);
    }

    @Test
    public void testUserDuplicate() {
        ContentValues values = new ContentValues();
        values.put("username", "user1");
        values.put("password", "pass1");
        long result1 = db.insert(HabitDbHelper.TABLE_USER, null, values);
        assertNotEquals(-1, result1);

        long result2 = db.insert(HabitDbHelper.TABLE_USER, null, values);
        assertEquals(-1, result2); // UNIQUE constraint
    }

    @Test
    public void testUserQuery() {
        ContentValues values = new ContentValues();
        values.put("username", "alice");
        values.put("password", "pw123");
        db.insert(HabitDbHelper.TABLE_USER, null, values);

        Cursor cursor = db.query(HabitDbHelper.TABLE_USER, null,
                "username=? AND password=?", new String[]{"alice", "pw123"},
                null, null, null);
        assertTrue(cursor.moveToFirst());
        assertEquals("alice", cursor.getString(cursor.getColumnIndexOrThrow("username")));
        cursor.close();
    }

    @Test
    public void testHabitInsert() {
        ContentValues values = new ContentValues();
        values.put("title", "运动");
        values.put("content", "跑步");
        values.put("imageuri", "");
        values.put("date", "2026/06/15");
        values.put("time", "08:30");
        long result = db.insert(HabitDbHelper.TABLE_HABIT, null, values);
        assertNotEquals(-1, result);
    }

    @Test
    public void testHabitUpdate() {
        ContentValues values = new ContentValues();
        values.put("title", "早起");
        values.put("content", "6点起床");
        values.put("date", "2026/06/15");
        values.put("time", "06:00");
        long id = db.insert(HabitDbHelper.TABLE_HABIT, null, values);
        assertNotEquals(-1, id);

        ContentValues updateValues = new ContentValues();
        updateValues.put("title", "早起 (修改)");
        int rows = db.update(HabitDbHelper.TABLE_HABIT, updateValues, "id=?",
                new String[]{String.valueOf(id)});
        assertEquals(1, rows);
    }

    @Test
    public void testHabitDelete() {
        ContentValues values = new ContentValues();
        values.put("title", "阅读");
        values.put("date", "2026/06/15");
        values.put("time", "12:00");
        long id = db.insert(HabitDbHelper.TABLE_HABIT, null, values);
        assertNotEquals(-1, id);

        int rows = db.delete(HabitDbHelper.TABLE_HABIT, "id=?",
                new String[]{String.valueOf(id)});
        assertEquals(1, rows);
    }

    @Test
    public void testHabitQueryAll() {
        ContentValues v1 = new ContentValues();
        v1.put("title", "运动");
        v1.put("date", "2026/06/15");
        v1.put("time", "08:00");
        db.insert(HabitDbHelper.TABLE_HABIT, null, v1);

        ContentValues v2 = new ContentValues();
        v2.put("title", "阅读");
        v2.put("date", "2026/06/15");
        v2.put("time", "12:00");
        db.insert(HabitDbHelper.TABLE_HABIT, null, v2);

        Cursor cursor = db.rawQuery("select * from " + HabitDbHelper.TABLE_HABIT, null);
        assertEquals(2, cursor.getCount());
        cursor.close();
    }
}
