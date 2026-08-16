package com.example.dailyhabittrackingapp.bean;

public class HabitBean {
    Integer id;
    String title;
    String content;
    String imageUri;
    String date;
    String time;
    boolean isFinished;
    int streak;

    public HabitBean() {
    }

    public HabitBean(Integer id, String title, String content, String imageUri, String date, String time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.date = date;
        this.time = time;
        this.isFinished = false;
        this.streak = 0;
    }

    public HabitBean(Integer id, String title, String content, String imageUri, String date, String time,
                     boolean isFinished) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.date = date;
        this.time = time;
        this.isFinished = isFinished;
        this.streak = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }
}
