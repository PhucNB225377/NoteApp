package com.example.note;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Update
    void updateNotes(List<Note> notes);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM note_table ORDER BY position ASC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note_table WHERE categoryId = :categoryId ORDER BY position ASC")
    LiveData<List<Note>> getNotesByCategory(int categoryId);

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Note>> searchAllNotes(String query);

    @Query("SELECT * FROM note_table WHERE categoryId = :categoryId AND title LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Note>> searchNotesByCategory(int categoryId, String query);

    @Query("SELECT * FROM note_table WHERE `check` = 1 ORDER BY position ASC")
    LiveData<List<Note>> getAllCheckedNotes();

    @Query("SELECT * FROM note_table WHERE `check` = 1 AND title LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Note>> searchAllCheckedNotes(String query);

    @Query("SELECT * FROM note_table WHERE `pin` = 1 ORDER BY position ASC")
    LiveData<List<Note>> getAllPinedNotes();

    @Query("SELECT * FROM note_table WHERE `pin` = 1 AND title LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Note>> searchAllPinedNotes(String query);

    @Query("SELECT * FROM note_table WHERE `check` = 0 ORDER BY position ASC")
    LiveData<List<Note>> getAllUncheckedNotes();

    @Query("SELECT * FROM note_table WHERE `check` = 0 AND title LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Note>> searchAllUncheckedNotes(String query);

    @Query("SELECT * FROM note_table WHERE categoryId = :categoryId AND `pin` = 1 ORDER BY position ASC")
    LiveData<List<Note>> getPinnedNotesByCategory(int categoryId);
}
