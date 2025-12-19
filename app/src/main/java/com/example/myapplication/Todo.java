package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName ="todo_table")
public class Todo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String content;
    private String account;
    private String description;
    private boolean isCompleted;
    private long timestamp;
    private long deadline;
    private long remindTime;
    private int sortOrder;//排序字段
@Ignore
    public Todo(String content,String account) {
        this.content = content;
        this.account=account;
        this.description="";
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
        this.sortOrder=0;
        this.deadline=0;
        this.remindTime=0;
    }
    public Todo(long id,String content,String description,boolean isCompleted,long timestamp,int sortOrder,long deadline,long remindTime){
        this.id=id;
        this.content = content;
        this.account="";
        this.description=description;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.sortOrder=sortOrder;
        this.deadline=deadline;
        this.remindTime=remindTime;
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
        description=in.readString();
        account=in.readString();
        if (content!=null&&content.isEmpty())content=null;
        if (description!=null&&description.isEmpty())description=null;
        isCompleted =in.readByte()!=0;
        timestamp=in.readLong();
        sortOrder=in.readInt();
        deadline=in.readLong();
        remindTime=in.readLong();
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
    public long getId() {
        return id;
    }
    public void setId(long id){
        this.id=id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp=timestamp;
    }
    public int getSortOrder(){
        return sortOrder;
    }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(long remindTime) {
        this.remindTime = remindTime;
    }
    public String getAccount(){
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public int describeContents(){

        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeLong(id);
        dest.writeString(content==null?"":content);
        dest.writeString(description==null?"":description);
        dest.writeString(account==null?"":account);
        dest.writeByte((byte)(isCompleted?1:0));
        dest.writeLong(timestamp);
        dest.writeInt(sortOrder);
        dest.writeLong(deadline);
        dest.writeLong(remindTime);

    }
}
