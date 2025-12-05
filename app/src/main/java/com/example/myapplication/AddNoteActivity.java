package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.Date;
public class AddNoteActivity extends AppCompatActivity {
    // 声明控件
    private EditText etTitle;   // 标题输入框
    private EditText etContent; // 内容输入框
    private Button btnAddNote;  // 添加按钮
    private NoteDatabase noteDatabase; // Room数据库实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载约束布局的界面
        setContentView(R.layout.add_note);

        // 初始化控件：通过ID找到布局中的控件
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnAddNote = findViewById(R.id.btn_add_note);
        // 获取数据库实例（单例模式，全局唯一）
        noteDatabase = NoteDatabase.getInstance(this);

        // 给“Add Note”按钮设置点击事件
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. 获取输入的标题和内容（trim()去除前后空格）
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();
                // 2. 获取当前时间（毫秒级，用于记录修改时间）
                long modifiedTime = new Date().getTime();

                // 3. 插入数据库：Room操作不能在主线程，所以开子线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 调用Dao的insertNote方法，插入新笔记
                        noteDatabase.noteDao().insertNote(new Note(title, content, modifiedTime));
                        // 插入完成后，回到列表页（需切回主线程操作UI）
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish(); // 关闭当前页面，返回上一级（NoteListActivity）
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
