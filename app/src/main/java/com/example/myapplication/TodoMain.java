package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TodoMain extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {
    public static TodoMain instance;
    public static List<Todo> todoList=new ArrayList<>();
    public static List<Todo> doneList = new ArrayList<>();
    public static final Object LIST_LOCK=new Object();//列表操作锁
    private TodoAdapter adapter;
    private EditText inputEditText;
    private ImageButton ibBack;
    private Switch switchHideDone;
    private TodoRepository repo;
    private TodoViewModel todoViewModel;
    private RecyclerView recyclerView;

    public static boolean isHideDone=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);
        instance = this;
        //初始化ui控件
        ibBack = findViewById(R.id.back);
        switchHideDone = findViewById(R.id.switch_hide_done);
        inputEditText = findViewById(R.id.input_edit_text);
        ImageButton addButton = findViewById(R.id.add_button);
        recyclerView = findViewById(R.id.recycler_view);
        //初始化数据库和仓库
        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        repo = new TodoRepository(todoDao);
        //初始化ViewModel
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);
        adapter = new TodoAdapter(todoList, this, todoViewModel);
        //初始化RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        //设置滑动删除
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete);
        if (deleteIcon != null) {
            deleteIcon.setBounds(0, 0, deleteIcon.getIntrinsicWidth(), deleteIcon.getIntrinsicHeight());
        }
        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(adapter, deleteIcon);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        //加载数据库数据(解决首次无数据)
        loadTodos();
        //隐藏已办开关
        switchHideDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideDone = isChecked;
                todoViewModel.isHideDone.postValue(isChecked);//同步到ViewModel
                refreshTodoList();
             /*   synchronized (LIST_LOCK) {
                    if (isChecked) {
                        //从todolist移动已完成项到doneList
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
                    } else {
                        todoList.addAll(doneList);
                        doneList.clear();
                    }
                    if (!recyclerView.isComputingLayout()) {
                        adapter.updateTodoList(new ArrayList<>(todoList));
                    }
                }*/
            }
        });
        // 添加待办事项
        addButton.setOnClickListener(v -> {
            String content = inputEditText.getText().toString().trim();
            if (!content.isEmpty()) {
                Todo newTodo = new Todo(content);
                newTodo.setCompleted(false);
                repo.addTodo(newTodo);
                //内存列表更新(主线程工作
            /*    synchronized (LIST_LOCK) {
                    todoList.add(0, newTodo);
                }
                if (!recyclerView.isComputingLayout()) {
                    adapter.updateTodoList(new ArrayList<>(todoList));
                }*/
                inputEditText.setText("");
                refreshTodoList();
                recyclerView.scrollToPosition(0);
            }
        });
        findViewById(R.id.donecheck).setOnClickListener(v -> {
            Intent intent = new Intent(TodoMain.this, DoneListActivity.class);
            startActivity(intent);
        });
        todoViewModel.isHideDone.observe(this, isHide -> {
          /*  if (adapter != null) {
                adapter.updateTodoList(getFilteredTodoList());
            }*/
            refreshTodoList();
        });
        setupClickListeners();
        loadTodos();
    }
    private void loadTodos() {
        repo.getAllTodos(todos -> {
            runOnUiThread(() -> {
                synchronized (LIST_LOCK) {
                    todoList.clear();
                    // todoList.addAll(todos);
                    doneList.clear();
                    for (Todo todo : todos) {
                        if (todo.isCompleted()) {
                            doneList.add(todo);
                        } else {
                            todoList.add(todo);
                        }
                    }
                    todoViewModel.resetLists(new ArrayList<>(todoList),new ArrayList<>(doneList));
                    if (!recyclerView.isComputingLayout()) {
                        adapter.updateTodoList(getFilteredTodoList());
                    }
                    /*if (isHideDone) {
                        Iterator<Todo> iterator = todoList.iterator();
                        while (iterator.hasNext()) {
                            Todo todo = iterator.next();
                            if (todo.isCompleted()) {
                                doneList.add(todo);
                                iterator.remove();
                            }
                        }
                    }
                    else{
                        todoList.addAll(doneList);
                        doneList.clear();
                    }
                }
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.updateTodoList(new ArrayList<>(todoList));
                        }
                    });*/
                }
            });
        });
    }
    private void setupClickListeners() {
        ibBack.setOnClickListener(v-> {
            Intent intent = new Intent(TodoMain.this, MainActivity.class);
            startActivity(intent);
        });
    }
    private List<Todo> getFilteredTodoList() {
        List<Todo> filteredList = new ArrayList<>();
        synchronized (LIST_LOCK) {
            if(isHideDone) {
             for(Todo item:todoList){
                 if(!item.isCompleted()){
                     filteredList.add(item);
                 }
              }
            }
            else{
                filteredList.addAll(todoList);
                filteredList.addAll(doneList);
                }
            }

        return filteredList;
    }
    public void refreshTodoList() {
        repo.getAllTodos(todos ->runOnUiThread(()->{
            synchronized (LIST_LOCK){
                todoList.clear();
                doneList.clear();
                for(Todo todo:todos){
                    if(todo.isCompleted()){
                        doneList.add(todo);
                    }
                    else{
                        todoList.add(todo);
                    }
                }
                todoViewModel.resetLists(new ArrayList<>(todoList),new ArrayList<>(doneList));
                if(isHideDone){
                    todoList.removeIf(Todo::isCompleted);
                }
                else{
                    todoList.addAll(doneList);
                    doneList.clear();
                }
            }
            if(!recyclerView.isComputingLayout()){
                adapter.updateTodoList(getFilteredTodoList());
            }
           /* if(!recyclerView.isComputingLayout()){
                adapter.updateTodoList(new ArrayList<>(todoList));
            }*/
        }));
     /*   List<Todo> filteredList = getFilteredTodoList();
        adapter.updateTodoList(filteredList);*/
    }
    @Override
    public void onDeleteClick(int position) {
        synchronized (LIST_LOCK) {
            List<Todo>filteredList=getFilteredTodoList();
            if (position >= 0 && position < filteredList.size()) {
                Todo todo = filteredList.get(position);
                //使用id找位置
                int realTodoPos = -1;
                int realDonePos = -1;
                synchronized (LIST_LOCK) {
                    for (int i = 0; i < todoList.size(); i++) {
                        if (todoList.get(i).getId() == todo.getId()) {
                            realTodoPos = i;
                            break;
                        }
                    }
                    for (int i = 0; i < doneList.size(); i++) {
                        if (doneList.get(i).getId() == todo.getId()) {
                            realDonePos = i;
                            break;
                        }
                    }
                }


                repo.deleteTodo(todo);
                synchronized (LIST_LOCK){
                    if (realTodoPos!=-1)todoList.remove(realTodoPos);
                    if (realDonePos!=-1)doneList.remove(realDonePos);
                }
                todoList.remove(todo);
                doneList.remove(todo);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position,filteredList.size()-position);
                refreshTodoList();
            }
        }
    }
    @Override
    public void onItemClick(int position) {
        synchronized (LIST_LOCK) {
            List<Todo> filteredList = getFilteredTodoList();
            if (position >= 0 && position < filteredList.size()) {
                Todo todo = filteredList.get(position);
                long todoId = todo.getId();
                Todo realTodo = null;
                boolean newState = !todo.isCompleted();
                /*
                todo.setCompleted(!todo.isCompleted());
                repo.updateTodo(todo); // 同步到数据库
                if (newState) {
                    todoList.remove(todo);
                    doneList.add(todo);
                } else {
                    doneList.remove(todo);
                    todoList.add(todo);
                }*/
                synchronized (LIST_LOCK) {
                    for (Todo t : todoList) {
                        if (t.getId() == todoId) {
                            realTodo = t;
                            break;
                        }
                    }
                    if (realTodo == null) {
                        for (Todo t : doneList) {
                            if (t.getId() == todoId) {
                                realTodo = t;
                                break;
                            }
                        }
                    }
                }

                if (realTodo != null) {
                    realTodo.setCompleted(newState);
                    repo.updateTodo(realTodo);
                    adapter.notifyItemChanged(position);
                    // 重新过滤列表（解决状态变更后不隐藏）
                    refreshTodoList();
                    todoViewModel.updateTodoState(realTodo, position, newState, adapter);
                    refreshTodoList();
                }
            }
        }
    }
    // 生命周期：返回页面时重新加载数据
    @Override
    protected void onResume() {
        super.onResume();
        refreshTodoList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null; // 防止内存泄漏
    }
}


