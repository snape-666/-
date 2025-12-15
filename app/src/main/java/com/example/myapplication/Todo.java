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
    private int sortOrder;//排序字段

    public Todo(String content) {
        this.content = content;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
        this.sortOrder=0;
    }
    @Override
    public boolean equals(Object object){
        if(this==object)return true;
        if(object==null||getClass()!=object.getClass())return false;
        Todo todo=(Todo) object;
        return id== todo.id;
    }
    @Override
    public int hashCode(){
        return (int) (id^(id>>>32));
    }
    protected Todo(Parcel in){
        id=in.readLong();
        content =in.readString();
        isCompleted =in.readByte()!=0;
        timestamp=in.readLong();
        sortOrder=in.readInt();
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
    public int getSortOrder(){return sortOrder;}
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeByte((byte)(isCompleted?1:0));
        dest.writeLong(timestamp);
        dest.writeInt(sortOrder);
    }
}
