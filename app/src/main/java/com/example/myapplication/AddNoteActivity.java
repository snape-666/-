package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Date;
public class AddNoteActivity extends AppCompatActivity {
    // 声明控件
    private EditText etTitle;
    private EditText etContent;
    private Button btnAddNote;
    private ImageButton ibBack;
    private NoteDatabase noteDatabase;
    private boolean isEditMode=false;
    private int editNoteId;

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

        Intent intent = getIntent();
        if (intent.hasExtra("NOTE_ID")) {
            isEditMode = true;
            editNoteId = intent.getIntExtra("NOTE_ID", -1);
            String title = intent.getStringExtra("NOTE_TITLE");
            String content = intent.getStringExtra("NOTE_CONTENT");
            etTitle.setText(title);
            etContent.setText(content);
            btnAddNote.setText("保存修改");
        }

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                if (title.isEmpty()) {
                    Toast.makeText(AddNoteActivity.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                long modifiedTime = new Date().getTime();

                Note note = new Note(title, content, modifiedTime);

                // 新增：编辑模式下设置ID并执行更新
                if (isEditMode) {
                    note.setId(editNoteId);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            noteDatabase.noteDao().updateNote(note);
                            runOnUiThread(() -> {
                                Toast.makeText(AddNoteActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    }).start();
                } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        noteDatabase.noteDao().insertNote(new Note(title, content, modifiedTime));
                        runOnUiThread(() -> {
                            Toast.makeText(AddNoteActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                }).start();
                }
            }
        });
    }
    private void setupClickListeners() {
        ibBack.setOnClickListener(v-> {
            Intent intent = new Intent(AddNoteActivity.this, NoteActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
