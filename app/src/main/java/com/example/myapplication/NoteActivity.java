package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import java.util.List;
public class NoteActivity extends AppCompatActivity {
    private RecyclerView rvNotes;
    private NoteAdapter noteAdapter;
    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note);

        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(this, null);
        rvNotes.setAdapter(noteAdapter);
        noteDatabase = NoteDatabase.getInstance(this);
        loadNotesFromDatabase();
        findViewById(R.id.ib_edit).setOnClickListener(v -> {
            startActivity(new Intent(NoteActivity.this, AddNoteActivity.class));
        });

    findViewById(R.id.back).setOnClickListener(v -> {
        startActivity(new Intent(NoteActivity.this, MainActivity.class));
    });
}

    @Override
    protected void onResume() {
        super.onResume();

        loadNotesFromDatabase();
    }

    private void loadNotesFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Note> notes = noteDatabase.noteDao().getAllNotes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        noteAdapter.refreshNotes(notes);
                    }
                });
            }
        }).start();
    }
}

