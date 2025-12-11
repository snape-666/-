package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TodoMain extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {
    public static TodoMain instance;
    private TodoAdapter adapter;
    private EditText inputEditText;
    private ImageButton ibBack;
    private Switch switchHideDone;
    private TodoRepository repo;
    public static List<Todo> todoList;
    public static List<Todo> doneList = new ArrayList<>();
    public static boolean isHideDone=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);

        TodoDatabase db=TodoDatabase.getInstance(this);
        TodoDao todoDao=db.getTodoDao();
        repo=new TodoRepository(todoDao);
        loadTodos();
        instance=this;
        ibBack = findViewById(R.id.back);
        switchHideDone = findViewById(R.id.switch_hide_done);
        setupClickListeners();
        todoList = new ArrayList<>();
        inputEditText = findViewById(R.id.input_edit_text);
        ImageButton addButton = findViewById(R.id.add_button);
        adapter = new TodoAdapter(todoList, this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        switchHideDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideDone=isChecked;
                if (isChecked) {
                    Iterator<Todo> iterator = todoList.iterator();
                    List<Todo> tempDone = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Todo todo = iterator.next();
                        if (todo.isCompleted()) {
                            tempDone.add(todo);
                            iterator.remove();
                        }
                    }

                    doneList.addAll(tempDone);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    // 刷新Todo页面的列表（比如RecyclerView的adapter.notifyDataSetChanged()）
                    refreshTodoList();

                }
            }
        });

        findViewById(R.id.donecheck).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到已办页面的Activity（比如DoneListActivity）
                Intent intent = new Intent(TodoMain.this, DoneListActivity.class);
                startActivity(intent);
            }
        });
       /* private void refreshTodoList() {
            // 示例：如果是RecyclerView，调用adapter.notifyDataSetChanged()
            // todoAdapter.notifyDataSetChanged();
        }*/




        // 设置拖拽和滑动删除
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete);
        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(adapter, deleteIcon);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // 添加待办事项
        addButton.setOnClickListener(v -> {
            String content = inputEditText.getText().toString().trim();
            if (!content.isEmpty()) {
               Todo newTodo=new Todo(content);
               // todoList.add(0, new Todo(content);
                adapter.notifyItemInserted(0);
                inputEditText.setText("");
                recyclerView.scrollToPosition(0);
                repo.addTodo(newTodo);
                loadTodos();
            }
        });
        repo.getAllTodos(todos -> {
            adapter.updateTodoList(todos);
        });
        // 示例数据
        todoList.add(new Todo("write something"));
        adapter.notifyDataSetChanged();

    }

    public void refreshTodoPage() {
        loadTodos();
    }
    private void loadTodos() {
        repo.getUndoneTodos(new TodoRepository.OnTodosLoadedListener() {
            @Override
            public void onLoaded(List<Todo> todos) {
                // 关键：用runOnUiThread切回主线程更新UI
                runOnUiThread(() -> {
                    adapter.updateTodoList(todos);
                });
            }
        });
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

    private List<Todo> getFilteredTodoList() {
        List<Todo> filteredList = new ArrayList<>();
        for (Todo item : todoList) {
            if (!isHideDone || !item.isCompleted()) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    public void refreshTodoList() {
        List<Todo> filteredList = getFilteredTodoList();
        adapter.updateTodoList(filteredList);
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
