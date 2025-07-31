package com.example.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note) { repository.insert(note); }
    public void update(Note note) { repository.update(note); }
    public void updateNotes(List<Note> notes) { repository.updateNotes(notes); }
    public void delete(Note note) { repository.delete(note); }

    public LiveData<List<Note>> getAllNotes() { return allNotes; }

    public LiveData<List<Note>> getNotesByCategory(int categoryId) { return repository.getNotesByCategory(categoryId); }

    public LiveData<List<Note>> searchAllNotes(String query) { return repository.searchAllNotes(query); }

    public LiveData<List<Note>> searchNotesByCategory(int categoryId, String query) { return repository.searchNotesByCategory(categoryId, query); }

    public LiveData<List<Note>> getAllCheckedNotes() { return repository.getAllCheckedNotes(); }

    public LiveData<List<Note>> searchAllCheckedNotes(String query) { return repository.searchAllCheckedNotes(query); }

    public LiveData<List<Note>> getAllPinedNotes() { return repository.getAllPinedNotes(); }

    public LiveData<List<Note>> searchAllPinedNotes(String query) { return repository.searchAllPinedNotes(query); }

    public LiveData<List<Note>> getAllUncheckedNotes() { return repository.getAllUncheckedNotes(); }

    public LiveData<List<Note>> searchAllUncheckedNotes(String query) { return repository.searchAllUncheckedNotes(query); }

    public LiveData<List<Note>> getPinedNotesByCategory(int categoryId) { return repository.getPinnedNotesByCategory(categoryId); }
}

