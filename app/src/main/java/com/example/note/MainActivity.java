package com.example.note;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    CategoryAdapter adapter;
    int swipedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new CategoryAdapter();
        recyclerView.setAdapter(adapter);

        CategoryViewModel categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        TextView tvEmptyCategory = findViewById(R.id.tvEmptyCategory);
        categoryViewModel.getAllCategories().observe(this, categories -> {
            adapter.setCategories(categories);
            if (categories.isEmpty()) {
                tvEmptyCategory.setVisibility(View.VISIBLE);
            } else {
                tvEmptyCategory.setVisibility(View.INVISIBLE);
            }
        });

        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    categoryViewModel.searchCategory(newText).observe(MainActivity.this, categories -> {
                        adapter.setCategories(categories);
                    });
                }
                return true;
            }
        });

        FloatingActionButton buttonAddCategory = findViewById(R.id.addCategory);
        buttonAddCategory.setOnClickListener(v -> {
            searchView.clearFocus();
            Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
            intent.putExtra("category_position", adapter.getItemCount());
            startActivity(intent);
        });

        adapter.setOnItemClickListener(category -> {
            searchView.clearFocus();
            Intent intent = new Intent(MainActivity.this, NoteListActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int from = viewHolder.getAdapterPosition();
                        int to = target.getAdapterPosition();

                        adapter.moveCategory(from, to);

                        return true;
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = viewHolder.itemView;

                            if (dX < 0) {
                                Paint paint = new Paint();
                                paint.setColor(Color.RED);
                                Drawable deleteIcon = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_delete);
                                assert deleteIcon != null;
                                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                                RectF background = new RectF(
                                        itemView.getRight() + dX, itemView.getTop(),
                                        itemView.getRight(), itemView.getBottom()
                                );
                                c.drawRect(background, paint);

                                int iconTop = itemView.getTop() + iconMargin;
                                int iconRight = itemView.getRight() - iconMargin;
                                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                deleteIcon.draw(c);

                            } else if (dX > 0) {
                                Paint paint = new Paint();
                                paint.setColor(Color.GRAY);
                                Drawable editIcon = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_edit);
                                assert editIcon != null;
                                int iconMargin = (itemView.getHeight() - editIcon.getIntrinsicHeight()) / 2;

                                RectF background = new RectF(
                                        itemView.getLeft(), itemView.getTop(),
                                        itemView.getLeft() + dX, itemView.getBottom()
                                );
                                c.drawRect(background, paint);

                                int iconTop = itemView.getTop() + iconMargin;
                                int iconLeft = itemView.getLeft() + iconMargin;
                                int iconRight = itemView.getLeft() + iconMargin + editIcon.getIntrinsicWidth();
                                int iconBottom = iconTop + editIcon.getIntrinsicHeight();
                                editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                editIcon.draw(c);
                            }
                        }
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        Category category = adapter.getCategoriesAt(viewHolder.getAdapterPosition());
                        if(direction == ItemTouchHelper.LEFT){
                            NoteViewModel noteViewModel = new ViewModelProvider(MainActivity.this).get(NoteViewModel.class);
                            noteViewModel.getNotesByCategory(category.getId()).observe(MainActivity.this, notes -> {
                                for (Note note : notes) {
                                    if (note.getReminderTimeMillis() != null) {
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, note.getAlarmRequestCode(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                                        alarmManager.cancel(pendingIntent);
                                        pendingIntent.cancel();
                                    }
                                }
                            });

                            categoryViewModel.delete(category);
                            Toast.makeText(MainActivity.this, "Đã xóa danh sách", Toast.LENGTH_SHORT).show();
                        }
                        else if(direction == ItemTouchHelper.RIGHT){
                            swipedPosition = viewHolder.getAdapterPosition();
                            Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
                            intent.putExtra("category_id", category.getId());
                            intent.putExtra("category_name", category.getName());
                            intent.putExtra("category_position", category.getPosition());
                            startActivity(intent);
                        }
                    }


                    @Override
                    public void clearView(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);

                        searchView.clearFocus();

                        List<Category> currentCategories = adapter.getCategories();
                        for (int i = 0; i < currentCategories.size(); i++) {
                            currentCategories.get(i).setPosition(i);
                        }
                        categoryViewModel.updateCategories(currentCategories);
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (swipedPosition != -1) {
            adapter.notifyItemChanged(swipedPosition);
            swipedPosition = -1;
        }
    }
}