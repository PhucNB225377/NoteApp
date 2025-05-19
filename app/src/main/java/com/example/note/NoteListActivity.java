package com.example.note;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

public class NoteListActivity extends AppCompatActivity {
    NoteAdapter adapter;
    NoteViewModel noteViewModel;
    int categoryId;
    int swipedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Intent intent1 = getIntent();
        categoryId = intent1.getIntExtra("category_id", -1);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewNote);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        TextView tvEmptyNote = findViewById(R.id.tvEmptyNote);
        noteViewModel.getNotesByCategory(categoryId).observe(this, notes -> {
            adapter.setNotes(notes);
            if (notes.isEmpty()) {
                tvEmptyNote.setVisibility(View.VISIBLE);
            } else {
                tvEmptyNote.setVisibility(View.INVISIBLE);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar1);
        toolbar.setTitle(intent1.getStringExtra("category_name"));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FloatingActionButton buttonAddNote = findViewById(R.id.addNote);
        buttonAddNote.setOnClickListener(v -> {
            toolbar.clearFocus();
            Intent intent = new Intent(NoteListActivity.this, NoteDetailActivity.class);
            intent.putExtra("note_position", adapter.getItemCount());
            intent.putExtra("category_id", categoryId);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(note -> {
            toolbar.clearFocus();
            Intent intent = new Intent(NoteListActivity.this, NoteDetailActivity.class);
            intent.putExtra("note_id", note.getId());
            intent.putExtra("note_position", note.getPosition());
            intent.putExtra("note_title", note.getTitle());
            intent.putExtra("note_content", note.getContent());
            intent.putExtra("category_id", categoryId);
            intent.putExtra("reminder_time_millis", note.getReminderTimeMillis());
            intent.putExtra("alarm_request_code", note.getAlarmRequestCode());
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

                        adapter.moveNote(from, to);

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
                        Note note = adapter.getNoteAt(viewHolder.getAdapterPosition());
                        if (direction == ItemTouchHelper.LEFT) {
                            if (note.getReminderTimeMillis() != null) {
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                Intent intent = new Intent(NoteListActivity.this, ReminderReceiver.class);
                                intent.putExtra("note_id", note.getId());
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(NoteListActivity.this, note.getAlarmRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                alarmManager.cancel(pendingIntent);
                                pendingIntent.cancel();
                            }

                            noteViewModel.delete(note);
                            Toast.makeText(NoteListActivity.this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                        }
                        else if (direction == ItemTouchHelper.RIGHT) {
                            swipedPosition = viewHolder.getAdapterPosition();
                            Intent intent = new Intent(NoteListActivity.this, NoteDetailActivity.class);
                            intent.putExtra("note_id", note.getId());
                            intent.putExtra("note_position", note.getPosition());
                            intent.putExtra("note_title", note.getTitle());
                            intent.putExtra("note_content", note.getContent());
                            intent.putExtra("category_id", categoryId);
                            intent.putExtra("reminder_time_millis", note.getReminderTimeMillis());
                            intent.putExtra("alarm_request_code", note.getAlarmRequestCode());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);

                        toolbar.clearFocus();

                        List<Note> currentNotes = adapter.getNotes();
                        for (int i = 0; i < currentNotes.size(); i++) {
                            currentNotes.get(i).setPosition(i);
                        }
                        noteViewModel.updateNotes(currentNotes);
                    }
                }
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (swipedPosition != -1) {
            adapter.notifyItemChanged(swipedPosition);
            swipedPosition = -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list_menu, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();
        assert searchView != null;
        searchView.setBackgroundColor(Color.WHITE);
        searchView.setQueryHint("Tìm ghi chú ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    noteViewModel.searchNotesByCategory(categoryId, newText).observe(NoteListActivity.this, notes -> {
                        adapter.setNotes(notes);
                    });
                }
                return true;
            }
        });

        return true;
    }
}