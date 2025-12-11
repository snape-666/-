package com.example.myapplication;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
@Database(entities = {Todo.class}, version = 1, exportSchema = false)
public abstract class TodoDatabase extends RoomDatabase {
    // 2. 单例实例：保证整个App只有一个数据库对象
    private static TodoDatabase instance;

    // 3. 抽象方法：返回TodoDao（Room会自动实现这个方法）
    public abstract TodoDao getTodoDao();

    // 4. 获取数据库实例（线程安全）
    public static synchronized TodoDatabase getInstance(Context context) {
        if (instance == null) {
            // 用Room.databaseBuilder创建数据库
            instance = Room.databaseBuilder(
                            context.getApplicationContext(), // 应用全局上下文
                            TodoDatabase.class, // 数据库类
                            "todo_db" // 数据库文件名（会存在手机存储中）
                    )
                    // 版本升级时直接删除旧数据（开发阶段用，正式环境需写Migration）
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}