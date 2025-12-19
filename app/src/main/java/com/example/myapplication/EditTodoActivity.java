package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTodoActivity extends AppCompatActivity {
    private Todo todo;
    private EditText etContent, etDesc;
    private TextView tvDeadline, tvRemindTime;
    private long deadline = 0;
    private long remindTime = 0;
    private TodoRepository repo;
    private long todoId;
    private String currentAccount;
    private final SimpleDateFormat deadlineSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);

        long todoId = getIntent().getLongExtra("TODO_ID", -1);
        if (todoId == -1) {
            Toast.makeText(this,"无效待办id",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindViews();
        initViews();
        new Thread(() -> {
            todo = TodoDatabase.getInstance(this).getTodoDao().getTodoById(todoId);
            runOnUiThread(() -> {
                if (todo == null) {
                    finish();
                    return;
                }

                etContent.setText(todo.getContent());
                etDesc.setText(todo.getDescription() != null ? todo.getDescription() : "");
                deadline = todo.getDeadline();
                remindTime = todo.getRemindTime();
                updateDeadlineText();
                updateRemindTimeText();
            });
        }).start();
    }

    private void bindViews(){
        etContent=findViewById(R.id.et_todo_content);
        etDesc=findViewById(R.id.et_todo_desc);
        tvDeadline=findViewById(R.id.tv_deadline);
        tvRemindTime=findViewById(R.id.tv_remind_time);
    }
    private void initViews(){
        tvDeadline.setOnClickListener(v -> showDateTimePicker(true));

        tvRemindTime.setOnClickListener(v -> showDateTimePicker(false));

        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        currentAccount = getSharedPreferences("user_info", MODE_PRIVATE).getString("account", "");


        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        repo = new TodoRepository(todoDao);


        todoId = getIntent().getLongExtra("TODO_ID", -1);
        if (todoId != -1) {
            loadTodoData();
        }
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请填写待办标题", Toast.LENGTH_SHORT).show();
                return;
            }

            if (todo == null) {
                Toast.makeText(this, "待办数据未加载完成", Toast.LENGTH_SHORT).show();
                return;
            }

            todo.setContent(content);
            todo.setDescription(etDesc.getText().toString().trim());
            todo.setDeadline(deadline);
            todo.setRemindTime(remindTime);

            if (todo.getAccount() == null || todo.getAccount().isEmpty()) {
                todo.setAccount(currentAccount);
            }

            new Thread(() -> {
                repo.updateTodo(todo);
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    TodoMain.shouldRefreshMain = true;
                    finish();
                });
            }).start();
        });


    }

    private void loadTodoData() {
        new Thread(() -> {
            Todo todo = TodoDatabase.getInstance(this).getTodoDao().getTodoById(todoId);
            runOnUiThread(() -> {
                if (todo != null) {
                    etContent.setText(todo.getContent());
                    etDesc.setText(todo.getDescription());

                    if (todo.getAccount() == null || todo.getAccount().isEmpty()) {
                        todo.setAccount(currentAccount);
                        repo.updateTodo(todo);
                    }
                }
            });
        }).start();
    }

    private void showDateTimePicker(boolean isDeadline) {
        Calendar calendar = Calendar.getInstance();
        if (isDeadline && deadline > 0) {
            calendar.setTimeInMillis(deadline);
        } else if (!isDeadline && remindTime > 0) {
            calendar.setTimeInMillis(remindTime);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute1) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year1, month1, dayOfMonth, hourOfDay, minute1);
                long selectedTime = selected.getTimeInMillis();
                long currentTime = System.currentTimeMillis();
                if (selectedTime <= currentTime) {
                    Toast.makeText(this, "请选择未来的时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isDeadline) {
                    deadline = selected.getTimeInMillis();
                    updateDeadlineText();
                } else {
                    remindTime = selected.getTimeInMillis();
                    updateRemindTimeText();
                }
            }, hour, minute, true);
            timePickerDialog.show();
        }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDeadlineText() {
        if (deadline > 0) {
            tvDeadline.setText("截止日期：" + deadlineSdf.format(deadline));
        } else {
            tvDeadline.setText("选择截止日期");
        }
    }
    private void updateRemindTimeText() {
        if (remindTime > 0) {
            tvRemindTime.setText("提醒时间：" + deadlineSdf.format(remindTime));
        } else {
            tvRemindTime.setText("选择提醒时间（可选）");
        }
    }

}