package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao // 标记为Room的Dao接口
public interface NoteDao {
    @Insert // 插入操作，Room自动生成SQL
    void insertNote(Note note);

    // 查询所有笔记，按修改时间倒序排列
    @Query("SELECT * FROM notes ORDER BY modifiedTime DESC")
    List<Note> getAllNotes();
}
