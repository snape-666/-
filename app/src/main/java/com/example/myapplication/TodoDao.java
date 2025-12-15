package com.example.myapplication;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
@Dao
public interface TodoDao {
    // 2. 插入待办：@Insert是Room内置注解，自动执行INSERT语句
    @Insert
    void insertTodo(Todo todo);

    // 3. 更新待办：@Update自动执行UPDATE语句（根据主键匹配待办）
    @Update
    void updateTodo(Todo todo);

    // 4. 删除待办：@Delete自动执行DELETE语句（根据主键匹配待办）
    @Delete
    void deleteTodo(Todo todo);

    // 5. 查询所有待办：@Query写自定义SQL，按时间戳倒序（最新的待办排在前面）
    @Query("SELECT * FROM todo_table ORDER BY timestamp DESC")//DESC降序,ASC升序
    List<Todo> getAllTodos();

    // 6. 查询已完成的待办：筛选isCompleted=true的记录
    @Query("SELECT * FROM todo_table WHERE isCompleted = 1 ORDER BY timestamp DESC")
    List<Todo> getDoneTodos();

    // 7. 查询未完成的待办：筛选isCompleted=false的记录
    @Query("SELECT * FROM todo_table WHERE isCompleted = 0 ORDER BY timestamp DESC")
    List<Todo> getUndoneTodos();

}
