package com.example.note;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "note_table",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("categoryId")}
)
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int position;
    private final String title;
    private final String content;
    private int categoryId;
    @Nullable
    private Long reminderTimeMillis;
    private int alarmRequestCode;

    public Note(String title, String content, int position) {
        this.title = title;
        this.content = content;
        this.position = position;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    public int getCategoryId() {
        return categoryId;
    }

    public void setReminderTimeMillis(@Nullable Long reminderTimeMillis) {
        this.reminderTimeMillis = reminderTimeMillis;
    }
    @Nullable
    public Long getReminderTimeMillis() {
        return reminderTimeMillis;
    }

    public int getAlarmRequestCode() {
        return alarmRequestCode;
    }
    public void setAlarmRequestCode(int alarmRequestCode) {
        this.alarmRequestCode = alarmRequestCode;
    }
}

