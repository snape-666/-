package com.example.myapplication;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

public class TodoRepository {
    private final TodoDao todoDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface TodoCallback<T> {
        void onResult(T result);
    }

    public TodoRepository(TodoDao todoDao) {
        this.todoDao = todoDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    public void getTodosByAccountSorted(String account, OnTodosLoadedListener listener) {
        executorService.execute(() -> {
            List<Todo> todos = todoDao.getTodosByAccountSorted(account);
            mainHandler.post(() -> listener.onLoaded(todos));
        });
    }
    public void getAllTodosSorted(OnTodosLoadedListener listener){
        executorService.execute(()->{
           List<Todo>todos=todoDao.getAllTodosSorted();
           mainHandler.post(()->listener.onLoaded(todos));
        });
    }


    public void addTodoWithAccount(Todo todo, String account) {
        new Thread(() -> {
            todo.setAccount(account);
            todoDao.insertTodo(todo);
        }).start();
    }

    public void updateTodo(Todo todo) {

        executorService.execute(() -> todoDao.updateTodo(todo));
    }

    // 删除待办
    public void deleteTodo(Todo todo) {

        executorService.execute(() -> todoDao.deleteTodo(todo));
    }

    public void getDoneTodos(OnTodosLoadedListener listener) {
        executorService.execute(() -> {
            List<Todo> doneTodos = todoDao.getDoneTodos();
            listener.onLoaded(doneTodos);
        });
    }

    public interface OnTodosLoadedListener {
        void onLoaded(List<Todo> todos);
    }

    public void getTodosByAccount(String account, TodoCallback<List<Todo>> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Todo> todos = todoDao.getTodosByAccount(account); // 需在 TodoDao 中定义该方法
            callback.onResult(todos);
        });
    }

}

