package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName ="todo_table")
public class Todo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String content;
    private boolean isCompleted;
    private long timestamp;

    public Todo(String content) {
        this.content = content;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
    }
    protected Todo(Parcel in){
        content =in.readString();
        isCompleted =in.readByte()!=0;
        timestamp=in.readLong();
    }
    public static final Creator<Todo> CREATOR =new Creator<Todo>() {
        @Override
        public Todo createFromParcel(Parcel in) {
            return new Todo(in);
        }

        @Override
        public Todo[] newArray(int size) {
            return new Todo[size];
        }
    };

    // getter和setter方法
    public long getId() {return id;}
    public void setId(long id){this.id=id;}
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp){this.timestamp=timestamp;}

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(content);
        dest.writeByte((byte)(isCompleted?1:0));
        dest.writeLong(timestamp);
    }
}
