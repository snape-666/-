package com.example.myapplication;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
@Dao
public interface TodoDao {
    @Insert
    void insertTodo(Todo todo);

    @Update
    void updateTodo(Todo todo);

    @Delete
    void deleteTodo(Todo todo);

    @Query("SELECT * FROM todo_table ORDER BY timestamp DESC")//DESC降序,ASC升序
    List<Todo> getAllTodos();

    @Query("SELECT * FROM todo_table WHERE isCompleted = 1 ORDER BY timestamp DESC")
    List<Todo> getDoneTodos();
    @Query("SELECT*FROM todo_table WHERE id=:id")
    Todo getTodoById(long id);

    @Query("SELECT*FROM todo_table WHERE isCompleted=0 AND (remindTime>:currentTime OR deadline>:currentTime) AND( remindTime>0 OR deadline>0)")
    List<Todo>getTodosNeedRemind(long currentTime);
    @Query("SELECT * FROM todo_table WHERE account = :account ORDER BY timestamp DESC")
    List<Todo> getTodosByAccount(String account);
    @Query("SELECT * FROM todo_table WHERE account = :account AND isCompleted = 0 ORDER BY timestamp DESC")
    List<Todo> getUncompletedTodosByAccount(String account);
    @Query("SELECT * FROM todo_table WHERE account = :account AND isCompleted = :isCompleted ORDER BY timestamp DESC")
    List<Todo> getTodosByAccountAndStatus(String account, boolean isCompleted);
    @Query("SELECT*FROM todo_table WHERE account=:account ORDER BY sortOrder ASC")
    List<Todo>getTodosByAccountSorted(String account);
    @Query("SELECT*FROM todo_table ORDER BY sortOrder ASC")
    List<Todo>getAllTodosSorted();
}