package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "mySQLite.db";
    private static final String TABLE_NAME_USER = "User";

    private static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME_USER + " ("
            + "id integer primary key autoincrement,"
            + "username text unique,"
            + "password text)";

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("User", null, values);
        db.close();
        return result != -1;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("User", null, "username like ?", new String[]{username}, null, null, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        db.close();
        return exists;
    }

    public boolean login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        boolean login = false;
        Cursor userCursor = db.query("User", null, "username = ? AND password = ?",
                new String[]{username, password}, null, null, null);
        if (userCursor == null) {
            db.close();
            return false;
        }

        if (userCursor.getCount() == 0) {
            userCursor.close();
            return false;
        }
        userCursor.close();
        Cursor cursor = db.query("User", null, "username = ? AND password = ?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            login = true;
            cursor.close();
        }

        db.close();
        return login;
    }
    public String getOriginalPassword(String username){
        SQLiteDatabase db=getReadableDatabase();
        String origPassword="";
        Cursor cursor=db.query("User",new String[]{"password"},"username=?",new String[]{username},null,null,null);
        if(cursor!=null&&cursor.moveToFirst()){
            origPassword=cursor.getString(cursor.getColumnIndexOrThrow("password"));
            cursor.close();
        }
        db.close();
        return origPassword;
    }
    public boolean updatePassword(String username,String newPassword){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("password",newPassword);
        //根据用户名更新密码
        int rowsAffected=db.update("User",values,"username=?",new String[]{username});
        db.close();
        //受影响数>0表示更新成功
        return rowsAffected>0;
    }

}

