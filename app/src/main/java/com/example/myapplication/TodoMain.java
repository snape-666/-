package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TodoMain extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {

    private List<Todo> todoList;
    private TodoAdapter adapter;
    private EditText inputEditText;
    private ImageButton ibBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);
        ibBack = findViewById(R.id.back);
        setupClickListeners();
        todoList = new ArrayList<>();
        inputEditText = findViewById(R.id.input_edit_text);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ImageButton addButton = findViewById(R.id.add_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);

        // 设置拖拽和滑动删除
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete);
        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(adapter, deleteIcon);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // 添加待办事项
        addButton.setOnClickListener(v -> {
            String content = inputEditText.getText().toString().trim();
            if (!content.isEmpty()) {
                todoList.add(0, new Todo(content));
                adapter.notifyItemInserted(0);
                inputEditText.setText("");
                recyclerView.scrollToPosition(0);
            }
        });

        // 示例数据
        todoList.add(new Todo("write something"));
        adapter.notifyDataSetChanged();

    }
    private void setupClickListeners() {


        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodoMain.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        todoList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemClick(int position) {
        // 可以实现点击编辑功能
        Todo todo = todoList.get(position);
        todo.setCompleted(!todo.isCompleted());
        adapter.notifyItemChanged(position);
    }
}
