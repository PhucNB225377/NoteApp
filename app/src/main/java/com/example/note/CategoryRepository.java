package com.example.note;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final LiveData<List<Category>> allCategories;
    private final ExecutorService executorService;

    public CategoryRepository(Application application){
        NoteDatabase database = NoteDatabase.getInstance(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAllCategories();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Category>> getAllCategories(){
        return allCategories;
    }

    public void insert(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void update(Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void delete(Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }

    public void updateCategories(List<Category> categories) {
        executorService.execute(() -> categoryDao.updateCategories(categories));
    }

    public LiveData<List<Category>> searchCategories(String query) {
        return categoryDao.searchCategories(query);
    }
}
