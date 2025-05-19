package com.example.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> allCategories;

    public CategoryViewModel(Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        allCategories = categoryRepository.getAllCategories();
    }

    public void insert(Category category){
        categoryRepository.insert(category);
    }

    public void update(Category category){
        categoryRepository.update(category);
    }

    public void delete(Category category){
        categoryRepository.delete(category);
    }

    public void updateCategories(List<Category> categories){
        categoryRepository.updateCategories(categories);
    }

    public LiveData<List<Category>> getAllCategories(){
        return allCategories;
    }

    public LiveData<List<Category>> searchCategory(String query){
        return categoryRepository.searchCategories("%" + query + "%");
    }
}
