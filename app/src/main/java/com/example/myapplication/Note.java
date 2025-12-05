package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes") // 标记为Room实体，对应数据库表“notes”
public class Note {
    @PrimaryKey(autoGenerate = true) // 主键，自动增长
    private int id;
    private String title;       // 笔记标题
    private String content;     // 笔记内容
    private long modifiedTime;  // 修改时间（毫秒）

    // 构造函数（Room需要无参构造或全参构造，这里用全参）
    public Note(String title, String content, long modifiedTime) {
        this.title = title;
        this.content = content;
        this.modifiedTime = modifiedTime;
    }

    // Getter和Setter方法（Room需要通过这些方法访问字段）
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(long modifiedTime) { this.modifiedTime = modifiedTime; }
}
