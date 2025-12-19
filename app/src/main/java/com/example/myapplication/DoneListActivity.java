package com.example.myapplication;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
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
        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        repo = new TodoRepository(todoDao);

        ImageButton backBtn = findViewById(R.id.back);
        recyclerView = findViewById(R.id.rv_done_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DoneAdapter(new ArrayList<>(), (item,position) -> {
            synchronized (TodoMain.LIST_LOCK) {
                item.setCompleted(false);
                repo.updateTodo(item);
                TodoMain.doneList.removeIf(t->t.getId()==item.getId());
                TodoMain.todoList.add(0,item);
                adapter.removeItem(item.getId());
                if (TodoMain.instance != null) {
                    TodoMain.instance.refreshTodoList();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        backBtn.setOnClickListener(v -> finish());
        loadDoneTodos();
    }

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
        loadDoneTodos();
    }

}





