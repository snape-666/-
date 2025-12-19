package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;

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
    private String currentAccount;

    public static boolean isHideDone=false;
    public AlarmManager alarmManager;
    public static boolean shouldRefreshMain=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);
        requestPermissions();

        currentAccount=getSharedPreferences("user_info",MODE_PRIVATE).getString("account","");
        Log.d("TodoMain","当前登录账号:"+currentAccount);


        alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()){
            Intent intent=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            Toast.makeText(this,"需开启精确闹钟权限才能准时提醒",Toast.LENGTH_SHORT).show();
        }

        instance = this;
        ibBack = findViewById(R.id.back);
        switchHideDone = findViewById(R.id.switch_hide_done);
        inputEditText = findViewById(R.id.input_edit_text);
        inputEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                |InputType.TYPE_TEXT_VARIATION_NORMAL);
        inputEditText.setHint("输入待办标题");
        ImageButton addButton = findViewById(R.id.add_button);
        recyclerView = findViewById(R.id.recycler_view);
        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        repo = new TodoRepository(todoDao);
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);
        adapter = new TodoAdapter(todoList, this, todoViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.delete);
        if (deleteIcon != null) {
            deleteIcon.setBounds(0, 0, deleteIcon.getIntrinsicWidth(), deleteIcon.getIntrinsicHeight());
        }
        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(adapter, deleteIcon) {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition=viewHolder.getBindingAdapterPosition();
                int toPosition=target.getBindingAdapterPosition();
                adapter.onItemMove(fromPosition,toPosition);
                return true;
            }
        };


        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        loadTodos();

        switchHideDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHideDone = isChecked;
                todoViewModel.isHideDone.postValue(isChecked);//同步到ViewModel
                refreshTodoList();
            }
        });
        addButton.setOnClickListener(v->{
            String content=inputEditText.getText().toString().trim();
            if(!content.isEmpty()){
                Todo newTodo=new Todo(content,currentAccount);
                newTodo.setCompleted(false);
                repo.addTodoWithAccount(newTodo,currentAccount);
                inputEditText.setText("");
                shouldRefreshMain=true;
                refreshTodoList();
                recyclerView.scrollToPosition(0);
            }
            else {
                Toast.makeText(this, "请输入待办内容", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.donecheck).setOnClickListener(v -> {
            Intent intent = new Intent(TodoMain.this, DoneListActivity.class);
            startActivity(intent);
        });
        todoViewModel.isHideDone.observe(this, isHide -> {
            refreshTodoList();
        });
        setupClickListeners();
        loadTodos();
        startReminderService();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("通知权限")
                        .setMessage("应用需要通知权限来提醒您待办事项")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(intent);
                        })
                        .setNegativeButton("稍后", null)
                        .show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("精确闹钟权限")
                        .setMessage("应用需要精确闹钟权限来准时提醒待办事项")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("稍后", null)
                        .show();
            }
        }
    }

    public void setReminderAlarm(Todo todo) {
        try {
            if (todo.isCompleted()) {
                cancelReminderAlarm(todo);
                Log.d("AlarmSet", "待办已完成，取消提醒: " + todo.getContent());
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (todo.getRemindTime() > 0 && todo.getRemindTime() > currentTime) {
                setSingleAlarm(todo, todo.getRemindTime(), "REMIND_TIME");
                Log.d("AlarmSet", "提醒时间闹钟设置成功: " + todo.getContent() +
                        " 触发时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(todo.getRemindTime()));
            }

            if (todo.getDeadline() > 0 && todo.getDeadline() > currentTime) {
                setSingleAlarm(todo, todo.getDeadline(), "DEADLINE");
                Log.d("AlarmSet", "截止时间闹钟设置成功: " + todo.getContent() +
                        " 触发时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(todo.getDeadline()));
            }
        } catch (Exception e) {
            Log.e("AlarmSet", "设置闹钟失败: " + todo.getContent(), e);
        }
    }

    private void setSingleAlarm(Todo todo, long triggerTime, String alarmType) {
        int requestCode = getAlarmRequestCode(todo.getId(), alarmType);
        Intent oldIntent = new Intent(this, TodoReminderReceiver.class);
        PendingIntent oldPendingIntent = PendingIntent.getBroadcast(
                this, requestCode, oldIntent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (oldPendingIntent != null) {
            alarmManager.cancel(oldPendingIntent);
            oldPendingIntent.cancel();
        }

        Intent newIntent = new Intent(this, TodoReminderReceiver.class);
        newIntent.putExtra("todo_id", todo.getId());
        newIntent.putExtra("todo_content", todo.getContent());
        newIntent.putExtra("alarm_type", alarmType); // 标记是提醒时间/截止时间
        newIntent.setAction("com.example.myapplication.TODO_REMINDER_" + todo.getId() + "_" + alarmType);

        PendingIntent newPendingIntent = PendingIntent.getBroadcast(this, requestCode, newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime, newPendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    newPendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    newPendingIntent
            );
        }
    }

    private int getAlarmRequestCode(long todoId, String alarmType) {
        int baseCode = (int) (todoId & Integer.MAX_VALUE);
        return alarmType.equals("REMIND_TIME") ? baseCode + 10000 : baseCode + 20000;
    }


    private void cancelReminderAlarm(Todo todo){
        int remindCode = getAlarmRequestCode(todo.getId(), "REMIND_TIME");
        PendingIntent remindIntent = PendingIntent.getBroadcast(
                this, remindCode, new Intent(this, TodoReminderReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (remindIntent != null) {
            alarmManager.cancel(remindIntent);
            remindIntent.cancel();
        }

        int deadlineCode = getAlarmRequestCode(todo.getId(), "DEADLINE");
        PendingIntent deadlineIntent = PendingIntent.getBroadcast(
                this, deadlineCode, new Intent(this, TodoReminderReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (deadlineIntent != null) {
            alarmManager.cancel(deadlineIntent);
            deadlineIntent.cancel();
        }
        Log.d("AlarmCancel", "待办所有闹钟已取消: " + todo.getContent());
    }

    private void startReminderService() {
        new Thread(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                TodoDatabase db = TodoDatabase.getInstance(this);
                TodoDao todoDao = db.getTodoDao();
                List<Todo> allTodos = todoDao.getAllTodos();
                int remindCount = 0;

                for (Todo todo : allTodos) {

                    if (!todo.isCompleted()) {
                        long remindTime = todo.getRemindTime();
                        long deadline = todo.getDeadline();
                        if ((remindTime > 0 && remindTime > currentTime) || (deadline > 0 && deadline > currentTime)) {
                            setReminderAlarm(todo);
                            remindCount++;
                        }
                    }
                }
                Log.d("ReminderService", "启动提醒服务，待提醒待办数: " + remindCount);
            } catch (Exception e) {
                Log.e("ReminderService", "启动提醒服务失败", e);
            }
        }).start();
    }

    private void loadTodos() {
        repo.getTodosByAccountSorted(currentAccount,todos -> {
            runOnUiThread(() -> {
                synchronized (LIST_LOCK) {
                    todoList.clear();
                    doneList.clear();
                    for (Todo todo : todos) {
                        if (todo.getAccount()==null||todo.getAccount().isEmpty()){
                            todo.setAccount(currentAccount);
                            repo.updateTodo(todo);
                        }
                        if (todo.isCompleted()) {
                            doneList.add(todo);

                            cancelReminderAlarm(todo);
                        } else {
                            todoList.add(todo);

                            setReminderAlarm(todo);
                        }
                    }
                    todoViewModel.resetLists(new ArrayList<>(todoList),new ArrayList<>(doneList));
                    if (!recyclerView.isComputingLayout()) {
                        adapter.updateTodoList(getFilteredTodoList());
                    }
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
        repo.getAllTodosSorted(todos ->runOnUiThread(()->{
            synchronized (LIST_LOCK){
                todoList.clear();
                doneList.clear();
                for(Todo todo:todos){
                    if(todo.isCompleted()){
                        doneList.add(todo);
                        cancelReminderAlarm(todo);
                    }
                    else{
                        todoList.add(todo);

                        setReminderAlarm(todo);
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
                adapter.notifyDataSetChanged();
            }
        }));
    }

    @Override
    public void onDeleteClick(int position) {
        synchronized (LIST_LOCK) {
            List<Todo>filteredList=getFilteredTodoList();
            if (position >= 0 && position < filteredList.size()) {
                Todo todo = filteredList.get(position);
                cancelReminderAlarm(todo);

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
                shouldRefreshMain=true;
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
                Intent intent = new Intent(this, EditTodoActivity.class);
                intent.putExtra("TODO_ID",todo.getId());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onCheckboxClick(int position, boolean newState) {
        synchronized (LIST_LOCK){
            List<Todo>filteredList=getFilteredTodoList();
            if (position>=0&&position<filteredList.size()){
                Todo todo=filteredList.get(position);
                todo.setCompleted(newState);
                repo.updateTodo(todo);
                shouldRefreshMain=true;

                if (newState) {
                    cancelReminderAlarm(todo);
                } else {
                    setReminderAlarm(todo);
                }
                adapter.notifyItemChanged(position);
                refreshTodoList();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTodoList();
        TodoMain.shouldRefreshMain = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}