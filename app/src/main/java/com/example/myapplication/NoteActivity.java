package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.List;
public class NoteActivity extends AppCompatActivity {
    private RecyclerView rvNotes;       // 笔记列表控件
    private NoteAdapter noteAdapter;    // 列表适配器
    private NoteDatabase noteDatabase;  // Room数据库实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载约束布局的界面
        setContentView(R.layout.activity_note);

        // 初始化RecyclerView
        rvNotes = findViewById(R.id.rv_notes);
        // 设置布局管理器：线性布局（垂直排列，和图片一致）
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        // 初始化适配器（先传空数据，后续加载）
        noteAdapter = new NoteAdapter(this, null);
        // 给RecyclerView设置适配器
        rvNotes.setAdapter(noteAdapter);

        // 初始化数据库实例
        noteDatabase = NoteDatabase.getInstance(this);

        // 加载笔记数据（从数据库查询）
        loadNotesFromDatabase();



        // （可选）给“添加笔记”按钮设置点击事件（比如用FloatingActionButton，图片中未显示但功能需要）
        // 这里假设布局中有一个FAB，点击跳转到AddNoteActivity
        findViewById(R.id.ib_edit).setOnClickListener(v -> {
            startActivity(new Intent(NoteActivity.this, AddNoteActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面恢复时重新加载数据（因为从AddNoteActivity返回后，数据可能更新）
        loadNotesFromDatabase();
    }

    // 从数据库加载笔记数据
    private void loadNotesFromDatabase() {
        // Room查询不能在主线程，开子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 调用Dao的getAllNotes方法，查询所有笔记（按修改时间倒序）
                List<Note> notes = noteDatabase.noteDao().getAllNotes();
                // 切回主线程更新UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 给适配器设置新数据，并刷新列表
                        noteAdapter.refreshNotes(notes);
                    }
                });
            }
        }).start();
    }
}

