package com.example.note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class NoteDetailActivity extends AppCompatActivity {
    private TextInputEditText edtTitle, edtContent;
    private TextView tvReminderTime;
    private Switch switchReminder;
    private NoteViewModel noteViewModel;
    private int noteId = -1;
    private int notePosition;
    private int categoryId;
    private String categoryName;
    private Long reminderTimeMillis = null;
    private int alarmRequestCode;

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        edtTitle = findViewById(R.id.edtNoteTitle);
        edtContent = findViewById(R.id.edtNoteContent);
        Button btnSave = findViewById(R.id.btnSaveNote);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        switchReminder = findViewById(R.id.switchReminder);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("note_position", -1);
        categoryId = intent.getIntExtra("category_id", -1);
        categoryName = intent.getStringExtra("category_name");

        if (intent.hasExtra("note_id")) {
            noteId = intent.getIntExtra("note_id", -1);
            edtTitle.setText(intent.getStringExtra("note_title"));
            edtContent.setText(intent.getStringExtra("note_content"));
            reminderTimeMillis = intent.getLongExtra("reminder_time_millis", -1);
            reminderTimeMillis = (reminderTimeMillis == -1) ? null : reminderTimeMillis;
            alarmRequestCode = intent.getIntExtra("alarm_request_code", -1);
        }

        if (reminderTimeMillis != null) {
            switchReminder.setChecked(true);
            tvReminderTime.setText("Nhắc lúc: " + formatMillisToDateTime(reminderTimeMillis));
            if (reminderTimeMillis < System.currentTimeMillis()) tvReminderTime.setTextColor(Color.RED);
            else tvReminderTime.setTextColor(Color.parseColor("#646464"));
            tvReminderTime.setVisibility(TextView.VISIBLE);
        } else {
            switchReminder.setChecked(false);
            tvReminderTime.setVisibility(TextView.GONE);
        }

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showDateTimePicker();
                if (reminderTimeMillis == null) { switchReminder.setChecked(false); }
            } else {
                tvReminderTime.setVisibility(TextView.GONE);
                reminderTimeMillis = null;
            }
        });

        btnSave.setOnClickListener(v -> {
            String title = Objects.requireNonNull(edtTitle.getText()).toString().trim();
            String content = Objects.requireNonNull(edtContent.getText()).toString().trim();

            if (title.isEmpty()) {
                edtTitle.setError("Nhập tiêu đề");
                return;
            }

            Note note = new Note(title, content, notePosition);
            note.setCategoryId(categoryId);
            note.setReminderTimeMillis(reminderTimeMillis);
            if (noteId == -1) {
                note.setAlarmRequestCode((int)System.currentTimeMillis());
                noteViewModel.insert(note);
            } else {
                note.setId(noteId);
                note.setAlarmRequestCode(alarmRequestCode);
                noteViewModel.update(note);
            }

            Intent intent1 = new Intent(NoteDetailActivity.this, ReminderReceiver.class);
            intent1.putExtra("note_id", note.getId());
            intent1.putExtra("note_title", title);
            intent1.putExtra("note_content", content);
            intent1.putExtra("category_id", categoryId);
            intent1.putExtra("category_name", categoryName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, note.getAlarmRequestCode(), intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (note.getReminderTimeMillis() != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.getReminderTimeMillis(), pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }

            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                reminderTimeMillis = calendar.getTimeInMillis();
                tvReminderTime.setText("Nhắc lúc: " + formatMillisToDateTime(reminderTimeMillis));
                if (reminderTimeMillis < System.currentTimeMillis()) tvReminderTime.setTextColor(Color.RED);
                else tvReminderTime.setTextColor(Color.parseColor("#646464"));
                tvReminderTime.setVisibility(TextView.VISIBLE);

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatMillisToDateTime(Long reminderTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm | dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(reminderTimeMillis));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
