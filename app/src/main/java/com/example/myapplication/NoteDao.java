package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insertNote(Note note);

    @Update
    void updateNote(Note note);
    @Query("SELECT * FROM notes ORDER BY modifiedTime DESC")
    List<Note> getAllNotes();
    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteNoteById(int noteId);

}
