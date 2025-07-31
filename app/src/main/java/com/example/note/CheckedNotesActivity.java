package com.example.note;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class CheckedNotesActivity extends AppCompatActivity {
    NoteAdapter noteAdapter;
    NoteViewModel noteViewModel;
    int swipedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_note);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewNote);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        TextView tvEmptyNote = findViewById(R.id.tvEmptyNote);
        noteViewModel.getAllCheckedNotes().observe(this, notes -> {
            noteAdapter.setNotes(notes);
            if (notes.isEmpty()) {
                tvEmptyNote.setVisibility(View.VISIBLE);
            } else {
                tvEmptyNote.setVisibility(View.INVISIBLE);
            }
        });

        Intent intent = getIntent();
        Toolbar toolbar = findViewById(R.id.toolbar1);
        toolbar.setTitle(intent.getStringExtra("category_name"));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                toolbar.clearFocus();
                Intent intent = new Intent(CheckedNotesActivity.this, NoteDetailActivity.class);
                intent.putExtra("note_id", note.getId());
                intent.putExtra("note_position", note.getPosition());
                intent.putExtra("note_title", note.getTitle());
                intent.putExtra("note_content", note.getContent());
                intent.putExtra("category_id", note.getCategoryId());
                intent.putExtra("reminder_time_millis", note.getReminderTimeMillis());
                intent.putExtra("alarm_request_code", note.getAlarmRequestCode());
                startActivity(intent);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onCheckClick(Note note) {
                toolbar.clearFocus();
                if (note.getCheck() == 1) note.setCheck(0);
                else note.setCheck(1);
                noteViewModel.update(note);
                noteAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPinClick(Note note) {
                toolbar.clearFocus();
                if (note.getPin() == 1) note.setPin(0);
                else note.setPin(1);
                noteViewModel.update(note);
                noteAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int from = viewHolder.getAdapterPosition();
                        int to = target.getAdapterPosition();

                        noteAdapter.moveNote(from, to);

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

                                RectF background = new RectF(
                                        itemView.getRight() + dX, itemView.getTop(),
                                        itemView.getRight(), itemView.getBottom()
                                );
                                c.drawRect(background, paint);

                                paint.setColor(Color.WHITE);
                                paint.setTextSize(40);
                                paint.setTextAlign(Paint.Align.CENTER);

                                float textX = itemView.getRight() - (itemView.getHeight() / 2f);
                                float textY = itemView.getTop() + (itemView.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2);

                                c.drawText("xóa", textX, textY, paint);

                            } else if (dX > 0) {
                                Paint paint = new Paint();
                                paint.setColor(Color.GRAY);

                                RectF background = new RectF(
                                        itemView.getLeft(), itemView.getTop(),
                                        itemView.getLeft() + dX, itemView.getBottom()
                                );
                                c.drawRect(background, paint);

                                paint.setColor(Color.WHITE);
                                paint.setTextSize(40);
                                paint.setTextAlign(Paint.Align.CENTER);

                                float textX = itemView.getLeft() + (itemView.getHeight() / 2f);
                                float textY = itemView.getTop() + (itemView.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2);

                                c.drawText("sửa", textX, textY, paint);
                            }
                        }
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        Note note = noteAdapter.getNoteAt(viewHolder.getAdapterPosition());
                        if (direction == ItemTouchHelper.LEFT) {
                            if (note.getReminderTimeMillis() != null) {
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                Intent intent = new Intent(CheckedNotesActivity.this, ReminderReceiver.class);
                                intent.putExtra("note_id", note.getId());
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(CheckedNotesActivity.this, note.getAlarmRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                alarmManager.cancel(pendingIntent);
                                pendingIntent.cancel();
                            }

                            noteViewModel.delete(note);
                            Toast.makeText(CheckedNotesActivity.this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                        }
                        else if (direction == ItemTouchHelper.RIGHT) {
                            swipedPosition = viewHolder.getAdapterPosition();
                            Intent intent = new Intent(CheckedNotesActivity.this, NoteDetailActivity.class);
                            intent.putExtra("note_id", note.getId());
                            intent.putExtra("note_position", note.getPosition());
                            intent.putExtra("note_title", note.getTitle());
                            intent.putExtra("note_content", note.getContent());
                            intent.putExtra("category_id", note.getCategoryId());
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

                        List<Note> currentNotes = noteAdapter.getNotes();
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
            noteAdapter.notifyItemChanged(swipedPosition);
            swipedPosition = -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_all_menu, menu);

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
                    noteViewModel.searchAllCheckedNotes(newText).observe(CheckedNotesActivity.this, notes -> {
                        noteAdapter.setNotes(notes);
                    });
                }
                return true;
            }
        });

        return true;
    }
}
