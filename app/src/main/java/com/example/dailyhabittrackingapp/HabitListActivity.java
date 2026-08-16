package com.example.dailyhabittrackingapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyhabittrackingapp.adaptor.HabitAdapter;
import com.example.dailyhabittrackingapp.bean.HabitBean;
import com.example.dailyhabittrackingapp.db.HabitDbHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitListActivity extends AppCompatActivity
        implements HabitAdapter.OnHabitDeleteListener, HabitAdapter.OnHabitFinishListener {

    private static final String TAG = "HabitApp";

    // Home page
    View btAdd;
    RecyclerView recyView;
    LinearLayout layoutEmpty;
    View pageHome;
    LinearProgressIndicator progressBar;
    TextView tvProgress;
    SearchView searchView;
    MaterialButtonToggleGroup toggleGroup;
    RecyclerView rvCalendar;
    LinearLayout layoutCalendar;
    TextView tvCalendarMonth;
    View btnResetToday;

    // Profile page
    View pageProfile;
    TextView tvProfileUsername;
    TextView tvProfileStreak;
    TextView tvStatTotal, tvStatToday, tvStatMaxStreak;
    SwitchCompat switchReminder;
    LinearLayout layoutReminderTime;
    TextView tvReminderTime;
    View itemLogout;
    View badgeBronze, badgeSilver, badgeGold;
    TextView tvBadgeBronze, tvBadgeSilver, tvBadgeGold;

    // DB
    HabitDbHelper dbHelper;
    SQLiteDatabase db;
    HabitAdapter adapter;
    List<HabitBean> arr;

    // User
    int currentUserId;

    // Navigation
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        // Get current user id
        currentUserId = getSharedPreferences("login", MODE_PRIVATE).getInt("user_id", -1);
        if (currentUserId == -1) {
            // Not logged in, go back to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Home views
        pageHome = findViewById(R.id.page_home);
        btAdd = findViewById(R.id.bt_add);
        recyView = findViewById(R.id.recy_view);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_indicator);
        tvProgress = findViewById(R.id.tv_progress);
        searchView = findViewById(R.id.search_view);
        toggleGroup = findViewById(R.id.toggle_group);
        rvCalendar = findViewById(R.id.rv_calendar);
        layoutCalendar = findViewById(R.id.layout_calendar);
        tvCalendarMonth = findViewById(R.id.tv_calendar_month);
        btnResetToday = findViewById(R.id.btn_reset_today);

        // Profile views
        pageProfile = findViewById(R.id.page_profile);
        tvProfileUsername = findViewById(R.id.tv_profile_username);
        tvProfileStreak = findViewById(R.id.tv_profile_streak);
        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatToday = findViewById(R.id.tv_stat_today);
        tvStatMaxStreak = findViewById(R.id.tv_stat_max_streak);
        switchReminder = findViewById(R.id.switch_reminder);
        layoutReminderTime = findViewById(R.id.layout_reminder_time);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        itemLogout = findViewById(R.id.item_logout);
        badgeBronze = findViewById(R.id.badge_bronze);
        badgeSilver = findViewById(R.id.badge_silver);
        badgeGold = findViewById(R.id.badge_gold);
        tvBadgeBronze = findViewById(R.id.tv_badge_bronze);
        tvBadgeSilver = findViewById(R.id.tv_badge_silver);
        tvBadgeGold = findViewById(R.id.tv_badge_gold);

        // DB
        dbHelper = new HabitDbHelper(this);
        db = dbHelper.getWritableDatabase();

        // RecyclerView with layout animation
        arr = new ArrayList<>();
        adapter = new HabitAdapter(arr, this, this, this);
        recyView.setLayoutManager(new LinearLayoutManager(this));
        LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyView.setLayoutAnimation(animController);
        recyView.setAdapter(adapter);

        // Bottom nav
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { showHomePage(); return true; }
            if (id == R.id.nav_mine) { showProfilePage(); return true; }
            return false;
        });

        // Add button
        btAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddHabitActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Reminder
        switchReminder.setOnCheckedChangeListener((btn, on) -> {
            getSharedPreferences("settings", MODE_PRIVATE).edit()
                    .putBoolean("reminder_enabled", on).apply();
            if (on) showTimePicker();
            else { layoutReminderTime.setVisibility(View.GONE); cancelReminder(); }
        });
        layoutReminderTime.setOnClickListener(v -> showTimePicker());
        itemLogout.setOnClickListener(v -> showLogoutDialog());

        // Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String q) { return false; }
            public boolean onQueryTextChange(String t) { adapter.filterByText(t); return true; }
        });

        // Segmented control
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_all) adapter.filterByStatus(0);
            else if (checkedId == R.id.btn_unfinished) adapter.filterByStatus(1);
            else if (checkedId == R.id.btn_finished) adapter.filterByStatus(2);
        });

        // Swipe delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder t) { return false; }
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= arr.size()) return;
                HabitBean bean = arr.get(pos);
                int habitId = bean.getId();
                db.delete(HabitDbHelper.TABLE_HABIT, "id=?",
                        new String[]{String.valueOf(habitId)});
                db.delete(HabitDbHelper.TABLE_CHECKIN, "habit_id=?",
                        new String[]{String.valueOf(habitId)});
                loadData();
                Snackbar.make(recyView, R.string.swipe_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.swipe_undo, v -> {
                        ContentValues cv = new ContentValues();
                        cv.put("user_id", currentUserId);
                        cv.put("title", bean.getTitle());
                        cv.put("content", bean.getContent());
                        cv.put("imageuri", bean.getImageUri());
                        cv.put("date", bean.getDate());
                        cv.put("time", bean.getTime());
                        db.insert(HabitDbHelper.TABLE_HABIT, null, cv);
                        loadData();
                    }).show();
            }
        }).attachToRecyclerView(recyView);

        // Reset today button
        btnResetToday.setOnClickListener(v -> showResetDialog());

        loadData();
        loadProfileData();
    }

    private void showHomePage() {
        pageHome.setVisibility(View.VISIBLE);
        pageProfile.setVisibility(View.GONE);
        pageHome.startAnimation(AnimationUtils.loadAnimation(this, R.anim.crossfade_in));
    }
    private void showProfilePage() {
        pageHome.setVisibility(View.GONE);
        pageProfile.setVisibility(View.VISIBLE);
        pageProfile.startAnimation(AnimationUtils.loadAnimation(this, R.anim.crossfade_in));
        loadProfileData();
    }

    // ====== Load ======
    @Override
    protected void onResume() { super.onResume(); loadData(); loadProfileData(); }

    @SuppressLint("Range")
    private void loadData() {
        arr.clear();
        Set<String> checkinSet = loadCheckinSet();
        String today = LocalDate.now().toString();

        Cursor c = db.rawQuery("select * from tb_habit where user_id=? order by id desc",
                new String[]{String.valueOf(currentUserId)});
        int fin = 0;
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("id"));
            String t = c.getString(c.getColumnIndex("title"));
            String ct = c.getString(c.getColumnIndex("content"));
            String img = c.getString(c.getColumnIndex("imageuri"));
            String d = c.getString(c.getColumnIndex("date"));
            String tm = c.getString(c.getColumnIndex("time"));

            // Check if today is checked in for this habit
            boolean todayFinished = isHabitCheckedIn(id, today);
            if (todayFinished) fin++;

            HabitBean bean = new HabitBean(id, t, ct, img, d, tm);
            bean.setFinished(todayFinished);
            // Calculate streak
            bean.setStreak(calcStreak(id));
            arr.add(bean);
        }
        c.close();
        adapter.updateList(new ArrayList<>(arr));
        if (arr.isEmpty()) { recyView.setVisibility(View.GONE); layoutEmpty.setVisibility(View.VISIBLE); }
        else { recyView.setVisibility(View.VISIBLE); layoutEmpty.setVisibility(View.GONE); }
        updateProgress(fin, arr.size());
        updateCalendar(checkinSet);
    }

    private boolean isHabitCheckedIn(int habitId, String date) {
        Cursor c = db.rawQuery("select id from " + HabitDbHelper.TABLE_CHECKIN +
                " where habit_id=? and checkin_date=? and user_id=?",
                new String[]{String.valueOf(habitId), date, String.valueOf(currentUserId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private Set<String> loadCheckinSet() {
        Set<String> set = new HashSet<>();
        try {
            Cursor c = db.rawQuery("select distinct checkin_date from tb_checkin where user_id=?",
                    new String[]{String.valueOf(currentUserId)});
            while (c.moveToNext()) set.add(c.getString(0));
            c.close();
        } catch (Exception ignored) {}
        return set;
    }

    private void updateProgress(int fin, int total) {
        if (total == 0) { progressBar.setProgressCompat(0, true); tvProgress.setText("✅ 0/0 已完成"); return; }
        int pct = (int)(fin * 100.0 / total);
        progressBar.setProgressCompat(pct, true);
        tvProgress.setText(getString(R.string.progress_format, fin, total));
    }

    // ====== Calendar ======
    private void updateCalendar(Set<String> checkinSet) {
        LocalDate now = LocalDate.now();
        tvCalendarMonth.setText(getString(R.string.calendar_month_format, now.getYear(), now.getMonthValue()));
        CalendarAdapter calAdapter = new CalendarAdapter(now.getYear(), now.getMonthValue(), checkinSet);
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendar.setAdapter(calAdapter);
        rvCalendar.setNestedScrollingEnabled(false);
    }

    // ====== Profile ======
    private void loadProfileData() {
        String name = getSharedPreferences("login", MODE_PRIVATE).getString("username", "用户");
        tvProfileUsername.setText(name);

        Cursor c = db.rawQuery("select count(*) from tb_habit where user_id=?",
                new String[]{String.valueOf(currentUserId)});
        int totalH = 0; if (c.moveToFirst()) totalH = c.getInt(0); c.close();

        int totalC = 0;
        try { Cursor c2 = db.rawQuery("select count(*) from tb_checkin where user_id=?",
                new String[]{String.valueOf(currentUserId)});
            if (c2.moveToFirst()) totalC = c2.getInt(0); c2.close(); } catch (Exception ignored) {}

        int maxStreak = calcMaxStreak();

        tvProfileStreak.setText(getString(R.string.profile_stats_format, totalH, totalC));
        tvStatTotal.setText(String.valueOf(totalH));
        tvStatToday.setText(String.valueOf(countTodayFinished()));
        tvStatMaxStreak.setText(String.valueOf(maxStreak));

        // Switches
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        boolean rem = sp.getBoolean("reminder_enabled", false);
        switchReminder.setChecked(rem);
        layoutReminderTime.setVisibility(rem ? View.VISIBLE : View.GONE);
        tvReminderTime.setText(sp.getString("reminder_time", "20:00"));

        // Badges
        boolean bronzeUnlocked = maxStreak >= 7;
        boolean silverUnlocked = maxStreak >= 30;
        boolean goldUnlocked = maxStreak >= 100;
        tvBadgeBronze.setText(bronzeUnlocked ? "✅ 坚持一周" : "坚持一周");
        tvBadgeSilver.setText(silverUnlocked ? "✅ 月度达人" : "月度达人");
        tvBadgeGold.setText(goldUnlocked ? "✅ 百日英雄" : "百日英雄");
        View ivBadgeBronze = findViewById(R.id.iv_badge_bronze);
        View ivBadgeSilver = findViewById(R.id.iv_badge_silver);
        View ivBadgeGold = findViewById(R.id.iv_badge_gold);
        ivBadgeBronze.setBackgroundResource(bronzeUnlocked ? R.drawable.bg_badge_on : R.drawable.bg_badge_off);
        ivBadgeSilver.setBackgroundResource(silverUnlocked ? R.drawable.bg_badge_on : R.drawable.bg_badge_off);
        ivBadgeGold.setBackgroundResource(goldUnlocked ? R.drawable.bg_badge_on : R.drawable.bg_badge_off);
        ivBadgeBronze.setAlpha(bronzeUnlocked ? 1f : 0.5f);
        ivBadgeSilver.setAlpha(silverUnlocked ? 1f : 0.5f);
        ivBadgeGold.setAlpha(goldUnlocked ? 1f : 0.5f);
    }

    private int countTodayFinished() {
        try {
            String today = LocalDate.now().toString();
            Cursor c = db.rawQuery("select count(distinct habit_id) from tb_checkin where user_id=? and checkin_date=?",
                    new String[]{String.valueOf(currentUserId), today});
            int n = 0; if (c.moveToFirst()) n = c.getInt(0); c.close();
            return n;
        } catch (Exception e) { return 0; }
    }

    private int calcMaxStreak() {
        int max = 0;
        try {
            Cursor c = db.rawQuery("select distinct habit_id from tb_checkin where user_id=?",
                    new String[]{String.valueOf(currentUserId)});
            while (c.moveToNext()) {
                int hid = c.getInt(0);
                int s = calcStreak(hid);
                if (s > max) max = s;
            }
            c.close();
        } catch (Exception ignored) {}
        return max;
    }

    private int calcStreak(int habitId) {
        try {
            Cursor c = db.rawQuery("select distinct checkin_date from tb_checkin where habit_id=? and user_id=? order by checkin_date desc",
                    new String[]{String.valueOf(habitId), String.valueOf(currentUserId)});
            int streak = 0;
            LocalDate expected = LocalDate.now();
            while (c.moveToNext()) {
                LocalDate d = LocalDate.parse(c.getString(0));
                if (d.equals(expected) || (d.equals(expected.minusDays(1)) && streak == 0)) {
                    streak++;
                    expected = d.minusDays(1);
                } else break;
            }
            c.close();
            return streak;
        } catch (Exception e) { return 0; }
    }

    // ====== Reminder ======
    private void showTimePicker() {
        String[] parts = getSharedPreferences("settings", MODE_PRIVATE)
                .getString("reminder_time", "20:00").split(":");
        int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
        new TimePickerDialog(this, (v, hh, mm) -> {
            String t = String.format(Locale.getDefault(), "%02d:%02d", hh, mm);
            tvReminderTime.setText(t);
            getSharedPreferences("settings", MODE_PRIVATE).edit().putString("reminder_time", t).apply();
            layoutReminderTime.setVisibility(View.VISIBLE);
            scheduleReminder(hh, mm);
        }, h, m, true).show();
    }

    private void scheduleReminder(int h, int m) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 100, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m); cal.set(Calendar.SECOND, 0);
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DAY_OF_MONTH, 1);
        if (am != null) try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } catch (SecurityException e) { Log.e(TAG, "Alarm denied", e); }
    }

    private void cancelReminder() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 100, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);
    }

    // ====== Logout ======
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("退出登录").setMessage("确定要退出当前账户吗？")
            .setPositiveButton("确定", (d, w) -> {
                getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply();
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }).setNegativeButton("取消", null).show();
    }

    // ====== Finish toggle (checkin for today) ======
    @Override
    public void onFinishToggle(int habitId, int position, boolean isFinished) {
        String today = LocalDate.now().toString();
        if (isFinished) {
            // Check in for today - insert if not exists
            Cursor c = db.rawQuery("select id from " + HabitDbHelper.TABLE_CHECKIN +
                    " where habit_id=? and checkin_date=? and user_id=?",
                    new String[]{String.valueOf(habitId), today, String.valueOf(currentUserId)});
            if (!c.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put("user_id", currentUserId);
                cv.put("habit_id", habitId);
                cv.put("checkin_date", today);
                db.insert(HabitDbHelper.TABLE_CHECKIN, null, cv);
            }
            c.close();
        } else {
            // Undo checkin for today
            db.delete(HabitDbHelper.TABLE_CHECKIN, "habit_id=? and checkin_date=? and user_id=?",
                    new String[]{String.valueOf(habitId), today, String.valueOf(currentUserId)});
        }
        arr.get(position).setFinished(isFinished);
        adapter.notifyItemChanged(position);
        loadData();
    }

    // ====== Delete ======
    @Override
    public void onDelete(int habitId, int position) {
        db.delete(HabitDbHelper.TABLE_HABIT, "id=?", new String[]{String.valueOf(habitId)});
        db.delete(HabitDbHelper.TABLE_CHECKIN, "habit_id=?", new String[]{String.valueOf(habitId)});
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        loadData();
    }

    // ====== Reset Today ======
    private void showResetDialog() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.confirm_reset_msg)
            .setPositiveButton(R.string.btn_confirm, (dialog, which) -> resetTodayCheckins())
            .setNegativeButton(R.string.btn_cancel, null)
            .show();
    }

    private void resetTodayCheckins() {
        String today = LocalDate.now().toString();
        db.delete(HabitDbHelper.TABLE_CHECKIN, "checkin_date=? AND user_id=?",
                new String[]{today, String.valueOf(currentUserId)});
        loadData();
        Toast.makeText(this, R.string.toast_reset_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
        if (dbHelper != null) dbHelper.close();
    }
}