package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class DoneListActivity extends AppCompatActivity {
    private TodoRepository repo;
    private RecyclerView recyclerView;
    private DoneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done); // 已办页面的布局
        TodoDatabase db=Room.databaseBuilder(getApplicationContext(),TodoDatabase.class,"todo_db").allowMainThreadQueries().build();
        TodoDao todoDao=db.getTodoDao();
        ImageButton backBtn = findViewById(R.id.back);

        recyclerView = findViewById(R.id.rv_done_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        repo = new TodoRepository(todoDao);
        adapter = new DoneAdapter(TodoMain.doneList, item -> {
            new Thread(() -> {
                TodoMain.doneList.remove(item);
                TodoMain.todoList.add(item);
                repo.updateTodo(item);
                runOnUiThread(() -> {
                    refreshDoneList();
                    if (TodoMain.instance != null) {
                        TodoMain.instance.refreshTodoList();
                    }
                    Toast.makeText(DoneListActivity.this, "已恢复到待办", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        recyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> finish());
    }

        @Override
        protected void onResume(){
        super.onResume();
        refreshDoneList();
        }
        private void refreshDoneList () {
            if (adapter != null) {
               // adapter.notifyDataSetChanged();
                adapter.updateDoneList(TodoMain.doneList);
            }
        }


        // 假设已办页面的列表项包含CheckBox（示例中绑定一个CheckBox，实际需结合列表循环处理）
  /*      CheckBox cbRestore = findViewById(R.id.rv_done_list);


        // 步骤4：复选框点击事件（恢复已办→移回todoList）
        cbRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbRestore.isChecked()) {
                    // 从doneList移除，添加到todoList
                    TodoMain.doneList.remove(targetItem);
                    TodoMain.todoList.add(todo);
                    // 刷新已办页面的列表
                    refreshDoneList();
                    // （可选）自动返回Todo页面
                    finish();
                }
            }
        });
    }


        // 辅助方法：刷新加载页面的列表
        private void refreshDoneList () {
            if (adapter != null) {
                adapter.notifyDataSetchanged();
            }
            // 示例：如果是RecyclerView，调用adapter.notifyDataSetChanged()
            // doneAdapter.notifyDataSetChanged();
        }*/

    }

