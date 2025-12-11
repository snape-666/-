package com.example.myapplication;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TodoRepository {
    private final TodoDao todoDao;
    // 线程池：Room操作必须在后台线程执行，这里用单线程池
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 构造方法：传入数据库的Dao
    public TodoRepository(TodoDao todoDao) {
        this.todoDao = todoDao;

    }
    public void getUndoneTodos(OnTodosLoadedListener listener) {
        executor.execute(() -> {
            List<Todo> undoneTodos = todoDao.getUndoneTodos(); // 调用Dao的查询方法
            listener.onLoaded(undoneTodos);
        });
    }
    // 1. 新增待办（异步）
    public void addTodo(Todo todo) {
        executor.execute(() -> todoDao.insertTodo(todo));
    }

    // 2. 更新待办（比如标记为已完成）
    public void updateTodo(Todo todo) {
        executor.execute(() -> todoDao.updateTodo(todo));
    }

    // 3. 删除待办
    public void deleteTodo(Todo todo) {
        executor.execute(() -> todoDao.deleteTodo(todo));
    }

    // 4. 获取所有待办（异步，用回调返回结果）
    public void getAllTodos(OnTodosLoadedListener listener) {
        executor.execute(() -> {
            List<Todo> todos = todoDao.getAllTodos();
            listener.onLoaded(todos); // 把结果通过回调给页面
        });
    }

    // 5. 获取已完成的待办
    public void getDoneTodos(OnTodosLoadedListener listener) {
        executor.execute(() -> {
            List<Todo> doneTodos = todoDao.getDoneTodos();
            listener.onLoaded(doneTodos);
        });
    }

    // 回调接口：传递异步查询的结果
    public interface OnTodosLoadedListener {
        void onLoaded(List<Todo> todos);
    }
}
