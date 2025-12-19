package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
public class TodoReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TODO_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "待办事项提醒";
    private static final int NOTIFICATION_ID_BASE = 1000; // 避免ID冲突
    private static final String TAG = "TodoReminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        final long todoId = intent.getLongExtra("todo_id", 0);
        final String[] todoContent = {intent.getStringExtra("todo_content")};
        final String alarmType = intent.getStringExtra("alarm_type");
        Log.d(TAG, "收到提醒:ID=" + todoId + "内容=" + todoContent[0]);
        new Thread(() -> {
            Todo todo = TodoDatabase.getInstance(context).getTodoDao().getTodoById(todoId);
            if (todo == null || todo.isCompleted()) {
                Log.d(TAG, "待办不存在/已完成，取消通知：ID=" + todoId);
                return;
            }

            if (todoContent[0] == null || todoContent[0].isEmpty()) {
                todoContent[0] = "您有未完成的待办事项";
            }
            String title = alarmType.equals("REMIND_TIME") ? "待办提醒时间到" : "待办截止时间到";
            String content = todoContent[0] + (alarmType.equals("REMIND_TIME") ? "（提醒时间已到）" : "（截止时间已到）");

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("待办事项截止/提醒通知");
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
            Intent editintent=new Intent(context,EditTodoActivity.class);
            editintent.putExtra("TODO_ID",todoId);
            editintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent=PendingIntent.getActivity(context,(int) todoId+(alarmType.equals("REMIND_TIME")?0:1000),
                    editintent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("待办事项提醒")
                    .setContentText(todoContent[0] + (todoId > 0 ? "（ID：" + todoId + "）" : "") + " 即将截止/已截止！")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true) // 点击后自动取消
                    .setVibrate(new long[]{0, 500, 500, 500})
                    .setDefaults(Notification.DEFAULT_SOUND);

            int notificationId = alarmType.equals("remind_time")
                    ? 10000 + (int) todoId
                    : 20000 + (int) todoId;
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "通知发送成功：" + content);
        }).start();
    }
}
