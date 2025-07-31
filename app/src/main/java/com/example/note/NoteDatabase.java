package com.example.note;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Note.class, Category.class}, version = 8)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase instance;
    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();

    private static final Callback roomCallback = new Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    };

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class, "note_database")
                    .addCallback(roomCallback)
                    .fallbackToDestructiveMigration(true)
                    .build();
        }
        return instance;
    }
}
