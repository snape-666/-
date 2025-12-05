package com.example.myapplication;

public class Todo {
    private String content;
    private boolean isCompleted;
    private long timestamp;

    public Todo(String content) {
        this.content = content;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
    }

    // getter和setter方法
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public long getTimestamp() { return timestamp; }
}
