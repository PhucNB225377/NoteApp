package com.example.note;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "category_table")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private final String name;
    private int position;

    public Category(String name, int position) {
        this.name = name;
        this.position = position;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public int getPosition() {
        return position;
    }
}
