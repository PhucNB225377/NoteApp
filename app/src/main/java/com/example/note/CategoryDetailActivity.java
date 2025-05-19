package com.example.note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class CategoryDetailActivity extends AppCompatActivity {
    private TextInputEditText edtName;
    private CategoryViewModel categoryViewModel;
    private int categoryId = -1;
    private int categoryPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        edtName = findViewById(R.id.edtCategoryName);
        Button btnSave = findViewById(R.id.btnSaveCategory);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        Intent intent = getIntent();
        categoryPosition = intent.getIntExtra("category_position", -1);
        if (intent.hasExtra("category_id")) {
            categoryId = intent.getIntExtra("category_id", -1);
            edtName.setText(intent.getStringExtra("category_name"));
        }

        btnSave.setOnClickListener(v -> {
            String name = Objects.requireNonNull(edtName.getText()).toString().trim();

            if (name.isEmpty()) {
                edtName.setError("Nhập tiêu đề");
                return;
            }

            Category category = new Category(name, categoryPosition);
            if (categoryId == -1) {
                categoryViewModel.insert(category);
            } else {
                category.setId(categoryId);
                categoryViewModel.update(category);
            }
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
