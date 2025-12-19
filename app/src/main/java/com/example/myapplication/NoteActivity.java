package com.example.myapplication;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
        rvNotes.setAdapter(noteAdapter);
        noteDatabase = NoteDatabase.getInstance(this);
        noteAdapter = new NoteAdapter(this, null, new NoteAdapter.OnNoteDeleteListener() {
            @Override
            public void onDeleteNote(int noteId) {
                // 子线程执行数据库删除操作
                new Thread(() -> {
                    noteDatabase.noteDao().deleteNoteById(noteId);
                    runOnUiThread(() -> {
                        noteAdapter.removeNoteById(noteId);
                        Toast.makeText(NoteActivity.this, "笔记已删除", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });
        rvNotes.setAdapter(noteAdapter);
        initItemTouchHelper();

        findViewById(R.id.ib_edit).setOnClickListener(v -> {
            startActivity(new Intent(NoteActivity.this, AddNoteActivity.class));
        });

    findViewById(R.id.back).setOnClickListener(v -> {
        startActivity(new Intent(NoteActivity.this, MainActivity.class));
        finish();
    });
        loadNotesFromDatabase();
}
    private void initItemTouchHelper() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (noteAdapter.getItemCount() == 0 || position < 0 || position >= noteAdapter.getItemCount()) {
                    noteAdapter.notifyItemChanged(position);
                    return;
                }

                //子线程获取笔记ID，避免主线程访问数据库
                new Thread(() -> {
                    List<Note> noteList = noteDatabase.noteDao().getAllNotes();
                    if (position >= noteList.size()) {
                        // 主线程恢复UI
                        runOnUiThread(() -> noteAdapter.notifyItemChanged(position));
                        return;
                    }
                    int noteId = noteList.get(position).getId();

                    // 主线程执行删除回调
                    runOnUiThread(() -> {
                        new androidx.appcompat.app.AlertDialog.Builder(NoteActivity.this)
                                .setTitle("确认删除")
                                .setMessage("是否确定删除这条笔记？")
                                .setPositiveButton("删除", (dialog, which) -> {
                                    noteAdapter.deleteListener.onDeleteNote(noteId);
                                })
                                .setNegativeButton("取消", (dialog, which) -> {
                                    noteAdapter.notifyItemChanged(position);
                                })
                                .show();
                    });
                }).start();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvNotes);
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

