package com.example.note;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;
    private final ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) { executorService.execute(() -> noteDao.insert(note)); }

    public void update(Note note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void updateNotes(List<Note> notes) {
        executorService.execute(() -> noteDao.updateNotes(notes));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getNotesByCategory(int categoryId) { return noteDao.getNotesByCategory(categoryId); }

    public LiveData<List<Note>> searchAllNotes(String query) { return noteDao.searchAllNotes(query); }

    public LiveData<List<Note>> searchNotesByCategory(int categoryId, String query) { return noteDao.searchNotesByCategory(categoryId, query); }

    public LiveData<List<Note>> getAllCheckedNotes() {
        return noteDao.getAllCheckedNotes();
    }

    public LiveData<List<Note>> searchAllCheckedNotes(String query) { return noteDao.searchAllCheckedNotes(query); }

    public LiveData<List<Note>> getAllPinedNotes() {
        return noteDao.getAllPinedNotes();
    }

    public LiveData<List<Note>> searchAllPinedNotes(String query) { return noteDao.searchAllPinedNotes(query); }

    public LiveData<List<Note>> getAllUncheckedNotes() {
        return noteDao.getAllUncheckedNotes();
    }

    public LiveData<List<Note>> searchAllUncheckedNotes(String query) { return noteDao.searchAllUncheckedNotes(query); }

    public LiveData<List<Note>> getPinnedNotesByCategory(int categoryId) { return noteDao.getPinnedNotesByCategory(categoryId); }
}
