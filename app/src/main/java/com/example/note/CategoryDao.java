package com.example.note;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Update
    void updateCategories(List<Category> categories);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM category_table ORDER BY position ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM category_table WHERE name LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Category>> searchCategories(String query);
}
