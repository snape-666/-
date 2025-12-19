package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Date;
public class AddNoteActivity extends AppCompatActivity {
    // 声明控件
    private EditText etTitle;
    private EditText etContent;
    private Button btnAddNote;
    private ImageButton ibBack;
    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_note);

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnAddNote = findViewById(R.id.btn_add_note);
        ibBack=findViewById(R.id.back);
        setupClickListeners();

        noteDatabase = NoteDatabase.getInstance(this);

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                long modifiedTime = new Date().getTime();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        noteDatabase.noteDao().insertNote(new Note(title, content, modifiedTime));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }).start();
            }
        });

    }
    private void setupClickListeners() {
        ibBack.setOnClickListener(v-> {
            Intent intent = new Intent(AddNoteActivity.this, NoteActivity.class);
            startActivity(intent);
        });
    }
}
