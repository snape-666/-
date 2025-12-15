package com.example.myapplication;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DoneListActivity extends AppCompatActivity {
    private TodoRepository repo;
    private RecyclerView recyclerView;
    private DoneAdapter adapter;
    private List<Todo> currentDoneList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        // ===== 修复点1：正确初始化数据库（不允许主线程操作）=====
        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        repo = new TodoRepository(todoDao);

        // 初始化UI
        ImageButton backBtn = findViewById(R.id.back);
        recyclerView = findViewById(R.id.rv_done_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ===== 修复点2：初始化Adapter时主动拉取已办数据 =====
        adapter = new DoneAdapter(new ArrayList<>(), (item,position) -> {
            //获取位置
            Log.d("DoneList","恢复项:"+item.getContent()+",位置:"+position);
            int restorePosition=currentDoneList.indexOf(item);
            if (restorePosition< 0 || restorePosition >= currentDoneList.size()) {
                Toast.makeText(this, "数据异常，无法恢复", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setCompleted(false);
            item.setSortOrder(0);



          /*  if(restorePosition==-1){
                currentDoneList.remove(restorePosition);
                adapter.notifyItemRemoved(restorePosition);
            }*/
            //更新todo状态并同步到数据库
            item.setCompleted(false);
            // 1. 同步到数据库（后台线程）
            repo.updateTodo(item);
            adapter.removeItem(position);
            // 2. 更新内存列表
            synchronized (TodoMain.LIST_LOCK) {
              /*  currentDoneList.remove(item);
                TodoMain.doneList.remove(item);
                TodoMain.todoList.add(item);*/
                TodoMain.doneList.removeIf(t->t.getId()==item.getId());
                currentDoneList.removeIf(t->t.getId()==item.getId());
                TodoMain.todoList.add(item);
            }
            adapter.removeItem(item.getId());
         /*   adapter.notifyItemRemoved(restorePosition);
            adapter.notifyItemRangeChanged(restorePosition,currentDoneList.size());
            //从内存列表移除
            synchronized (TodoMain.LIST_LOCK){
                TodoMain.doneList.remove(item);
                currentDoneList.remove(item);
                TodoMain.todoList.add(item);
            }*/
            // 3. 刷新UI
            runOnUiThread(() -> {
                refreshDoneList();
                if (TodoMain.instance != null) {
                    TodoMain.instance.refreshTodoList(); // 同步刷新待办页面
                }
                Toast.makeText(DoneListActivity.this, "已恢复到待办", Toast.LENGTH_SHORT).show();
            });
        });
        recyclerView.setAdapter(adapter);

        // 返回按钮
        backBtn.setOnClickListener(v -> finish());

        // 主动加载已办数据（解决首次无数据）
        loadDoneTodos();
    }

    // ===== 修复点3：主动拉取数据库中的已办数据 =====
    private void loadDoneTodos() {
        repo.getDoneTodos(todos -> {
            runOnUiThread(() -> {
                List<Todo>validDoneList=new ArrayList<>();
                for(Todo todo:todos){
                    if(todo.isCompleted()){
                        validDoneList.add(todo);
                    }
                }
                synchronized (TodoMain.LIST_LOCK) {
                    TodoMain.doneList.clear();
                    TodoMain.doneList.addAll(todos);
                    currentDoneList.clear();
                    currentDoneList.addAll(validDoneList);
                }
                adapter.updateDoneList(new ArrayList<>(TodoMain.doneList));
                Log.d("DoneList","加载已办项数量:"+validDoneList.size());
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoneTodos(); // 每次进入都刷新已办列表
    }

    private void refreshDoneList() {
        synchronized (TodoMain.LIST_LOCK) {
            adapter.updateDoneList(new ArrayList<>(TodoMain.doneList));
        }
    }
}
/*
import android.os.Bundle;
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

    }*/




