package com.example.note;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewTitle.setText(currentNote.getTitle());

        if (currentNote.getReminderTimeMillis() != null) {
            holder.tvReminderTime.setVisibility(View.VISIBLE);
            holder.tvReminderTime.setText("Nhắc lúc: " + formatMillisToDateTime(currentNote.getReminderTimeMillis()));
            if (currentNote.getReminderTimeMillis() < System.currentTimeMillis()) holder.tvReminderTime.setTextColor(Color.RED);
            else holder.tvReminderTime.setTextColor(Color.parseColor("#646464"));
        } else {
            holder.tvReminderTime.setVisibility(View.GONE);
        }

        holder.cbNote.setChecked(currentNote.getCheck() == 1);

        if (currentNote.getPin() == 1) {
            holder.pin.setColorFilter(Color.parseColor("#FFC107"));
        } else {
            holder.pin.setColorFilter(Color.parseColor("#AAAAAA"));
        }
    }

    private String formatMillisToDateTime(Long reminderTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm | dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(reminderTimeMillis));
    }

    @Override
    public int getItemCount() { return notes.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    public void moveNote(int from, int to) {
        if (from < getItemCount() && to < getItemCount()) {
            Note fromNote = notes.get(from);
            notes.remove(from);
            notes.add(to, fromNote);
            notifyItemMoved(from, to);
        }
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final CheckBox cbNote;
        private final TextView tvReminderTime;
        private final ImageView pin;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tvNoteTitle);
            cbNote = itemView.findViewById(R.id.cbNote);
            tvReminderTime = itemView.findViewById(R.id.tvReminderTime);
            pin = itemView.findViewById(R.id.pin);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(notes.get(position));
                }
            });

            cbNote.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCheckClick(notes.get(position));
                }
            });

            pin.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onPinClick(notes.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
        void onCheckClick(Note note);
        void onPinClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
