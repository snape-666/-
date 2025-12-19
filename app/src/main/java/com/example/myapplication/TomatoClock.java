package com.example.myapplication;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class TomatoClock extends AppCompatActivity {
    private static final String SP_NAME = "TomatoClockState";
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_REMAINING_TIME = "remaining_time";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_IS_PAUSED = "is_paused";
    private static final String KEY_CURRENT_TYPE = "current_type";
    private static final String KEY_SELECTED_DURATION = "selected_duration";

    private CircleProgress progressBar;
    private TextView timerText;
    private ImageButton tomatoBack;
    private CountDownTimer currentTimer;
    private long totalTimeMs;
    private long remainingTimeMs;
    private boolean isTimerRunning = false;
    private boolean isPaused = false;
    private int currentType = 0;
    private int selectedDuration = 30;
    private NumberPicker durationPicker;
    private TextView tvDuration;

    private static final String CHANNEL_ID = "TOMATO_CLOCK_CHANNEL";
    private static final int NOTIFICATION_ID = 1001;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);
        sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        tvDuration = findViewById(R.id.workBtn);
        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        tomatoBack = findViewById(R.id.back);
        ImageButton pauseResumeBtn = findViewById(R.id.giveUpBtn);
        TextView shortRestBtn = findViewById(R.id.shortRestBtn);
        TextView longRestBtn = findViewById(R.id.longRestBtn);
        createNotificationChannel();
        String[] durationOptions = {"30", "60", "90", "120", "150", "180", "210", "240", "自定义"};
        restoreTimerState();
        tvDuration.setOnClickListener(v -> {

            cancelTimer();
            currentType = 0;
            showDurationPickerDialog(durationOptions);
        });
        pauseResumeBtn.setOnClickListener(v -> {
            if (isTimerRunning) {
                if (isPaused) {
                    resumeTimer();
                    pauseResumeBtn.setBackgroundResource(R.drawable.pause);
                    isPaused = false;
                    saveTimerState();
                } else {
                    pauseTimer();
                    pauseResumeBtn.setBackgroundResource(R.drawable.pause);
                    isPaused = true;
                    saveTimerState();
                }
            } else {
                Toast.makeText(this, "请先选择时长并开始计时", Toast.LENGTH_SHORT).show();
            }
        });
        shortRestBtn.setOnClickListener(v -> {
            cancelTimer();
            currentType = 1;
            startTimer(5 * 60 * 1000, "短时休息");
            saveTimerState();
        });

        longRestBtn.setOnClickListener(v -> {
            cancelTimer();
            currentType = 2;
            startTimer(20 * 60 * 1000, "长时休息");
            saveTimerState();
        });

        tomatoBack.setOnClickListener(v -> {
            Toast.makeText(TomatoClock.this, "返回主页", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TomatoClock.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        updateDurationDisplay();
    }
    private void showDurationPickerDialog(String[] options) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_duration_picker, null);
        builder.setView(dialogView);

        durationPicker = dialogView.findViewById(R.id.duration_picker);
        durationPicker.setMinValue(0);
        durationPicker.setMaxValue(options.length - 1);
        durationPicker.setDisplayedValues(options);
        durationPicker.setValue(0);
        durationPicker.setWrapSelectorWheel(false);

        durationPicker.setOnScrollListener((view, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                int selectedIndex = durationPicker.getValue();
                if (selectedIndex == options.length - 1) {
                    showCustomDurationInput();
                    ((AlertDialog) builder.create()).dismiss();
                } else {
                    selectedDuration = Integer.parseInt(options[selectedIndex]);
                    updateDurationDisplay();
                }
            }
        });

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            int selectedIndex = durationPicker.getValue();
            if (selectedIndex != options.length - 1) {
                selectedDuration = Integer.parseInt(options[selectedIndex]);
                updateDurationDisplay();

                startTimer(selectedDuration * 60 * 1000,"专注");
                ((AlertDialog) builder.create()).dismiss();
                saveTimerState();
            }
        });

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            lp.y = 180;
            window.setAttributes(lp);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
    private void showCustomDurationInput() {
        EditText etCustom = new EditText(this);
        etCustom.setHint("请输入30-720分钟");
        etCustom.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCustom.setGravity(Gravity.CENTER);
        etCustom.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        etCustom.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("自定义专注时长")
                .setView(etCustom)
                .setPositiveButton("确定", (dialog, which) -> {
                    String input = etCustom.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(this, "请输入时长", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int customDuration ;
                    try {
                        customDuration = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (customDuration < 30 || customDuration > 720) {
                        Toast.makeText(this, "请输入30至720分钟区间内", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedDuration = customDuration;
                    updateDurationDisplay();
                    startTimer(selectedDuration * 60 * 1000,"专注");
                    dialog.dismiss();
                    saveTimerState();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startTimer(long durationMs,String type) {
        cancelTimer();
        totalTimeMs = durationMs;
        remainingTimeMs = durationMs;
        isTimerRunning = true;
        isPaused = false;

        long min = (remainingTimeMs / 1000) / 60;
        long sec = (remainingTimeMs / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", min, sec));
        progressBar.setProgress(0);

        new android.os.Handler().postDelayed(() -> {
                    currentTimer = new CountDownTimer(remainingTimeMs, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            remainingTimeMs = millisUntilFinished;
                            long min = (remainingTimeMs / 1000) / 60;
                            long sec = (remainingTimeMs / 1000) % 60;
                            timerText.setText(String.format("%02d:%02d", min, sec));
                            float progress = (1 - (float) remainingTimeMs / totalTimeMs) * 100;
                            progressBar.setProgress(progress);
                            saveTimerState();
                        }

                        @Override
                        public void onFinish() {
                            timerText.setText("00:00");
                            progressBar.setProgress(100);
                            isTimerRunning = false;
                            String finishText;
                            String notificationTitle;
                            if (currentType == 0) { // 专注完成
                                finishText = "已专注：" + selectedDuration + "分钟";
                                notificationTitle = "专注完成";
                            } else if (currentType == 1) { // 短时休息完成
                                finishText = "短时休息完成（5分钟）";
                                notificationTitle = "休息完成";
                            } else { // 长时休息完成
                                finishText = "长时休息完成（20分钟）";
                                notificationTitle = "休息完成";
                            }

                            Toast.makeText(TomatoClock.this, finishText, Toast.LENGTH_LONG).show();
                            showCompletionNotification(notificationTitle, finishText);

                            clearTimerState();
                        }
                    }.start();
                },100);

        findViewById(R.id.giveUpBtn).setBackgroundResource(R.drawable.pause);
    }

    private void pauseTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
    }

    private void resumeTimer() {
        // 修复：继续时先显示当前剩余时长，再启动倒计时
        long min = (remainingTimeMs / 1000) / 60;
        long sec = (remainingTimeMs / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", min, sec));

        new android.os.Handler().postDelayed(() -> {
            currentTimer = new CountDownTimer(remainingTimeMs, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    remainingTimeMs = millisUntilFinished;
                    long minTick = (remainingTimeMs / 1000) / 60;
                    long secTick = (remainingTimeMs / 1000) % 60;
                    timerText.setText(String.format("%02d:%02d", minTick, secTick));
                    float progress = (1 - (float) remainingTimeMs / totalTimeMs) * 100;
                    progressBar.setProgress(progress);
                    saveTimerState();
                }

                @Override
                public void onFinish() {
                    timerText.setText("00:00");
                    progressBar.setProgress(100);
                    isTimerRunning = false;
                    String finishText;
                    String notificationTitle;
                    if (currentType == 0) {
                        finishText = "已专注：" + selectedDuration + "分钟";
                        notificationTitle = "专注完成";
                    } else if (currentType == 1) {
                        finishText = "短时休息完成（5分钟）";
                        notificationTitle = "休息完成";
                    } else {
                        finishText = "长时休息完成（20分钟）";
                        notificationTitle = "休息完成";
                    }
                    Toast.makeText(TomatoClock.this, "已专注：" + selectedDuration + "分钟", Toast.LENGTH_LONG).show();
                    showCompletionNotification(notificationTitle, finishText);
                    clearTimerState();
                }
            }.start();
        }, 100);
    }

    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
        isTimerRunning = false;
        isPaused = false;
        timerText.setText("00:00");
        progressBar.setProgress(0);
        // 恢复暂停按钮图标
        findViewById(R.id.giveUpBtn).setBackgroundResource(R.drawable.pause);
    }

    private void updateDurationDisplay() {
        tvDuration.setText(selectedDuration + "分钟\n工作");
        sp.edit().putInt(KEY_SELECTED_DURATION, selectedDuration).apply();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "番茄钟完成提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("番茄钟专注完成通知");
            channel.enableVibration(true); // 开启震动
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void showCompletionNotification(String title,String content) {
        Intent intent = new Intent(this, TomatoClock.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("番茄钟完成")
                .setContentText("已专注：" + content + "分钟")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500});

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    private void saveTimerState() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_TOTAL_TIME, totalTimeMs);
        editor.putLong(KEY_REMAINING_TIME, remainingTimeMs);
        editor.putBoolean(KEY_IS_RUNNING, isTimerRunning);
        editor.putBoolean(KEY_IS_PAUSED, isPaused);
        editor.putInt(KEY_CURRENT_TYPE, currentType);
        editor.putInt(KEY_SELECTED_DURATION, selectedDuration);
        editor.apply();
    }

    private void restoreTimerState() {
        totalTimeMs = sp.getLong(KEY_TOTAL_TIME, 0);
        remainingTimeMs = sp.getLong(KEY_REMAINING_TIME, 0);
        isTimerRunning = sp.getBoolean(KEY_IS_RUNNING, false);
        isPaused = sp.getBoolean(KEY_IS_PAUSED, false);
        currentType = sp.getInt(KEY_CURRENT_TYPE, 0);
        selectedDuration = sp.getInt(KEY_SELECTED_DURATION, 30);


        updateDurationDisplay();
        if (remainingTimeMs > 0) {
            long min = (remainingTimeMs / 1000) / 60;
            long sec = (remainingTimeMs / 1000) % 60;
            timerText.setText(String.format("%02d:%02d", min, sec));
            float progress = (1 - (float) remainingTimeMs / totalTimeMs) * 100;
            progressBar.setProgress(progress);
        } else {
            timerText.setText("00:00");
            progressBar.setProgress(0);
        }

        ImageButton pauseResumeBtn = findViewById(R.id.giveUpBtn);
        if (isPaused) {
            pauseResumeBtn.setBackgroundResource(R.drawable.pause);
        } else {
            pauseResumeBtn.setBackgroundResource(R.drawable.pause);
        }

        if (isTimerRunning && !isPaused && remainingTimeMs > 0) {
            resumeTimer();
        }
    }

    private void clearTimerState() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }
    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }
}





