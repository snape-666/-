package com.example.myapplication;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

// 数据库类：指定实体、版本号
@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase instance; // 单例实例

    // 必须抽象方法，返回Dao接口（Room自动实现）
    public abstract NoteDao noteDao();

    // 获取单例实例（线程安全）
    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            // 构建数据库实例
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            "note_db" // 数据库文件名
                    )
                    .fallbackToDestructiveMigration() // 版本更新时销毁旧数据（测试用）
                    .build();
        }
        return instance;
    }
}