package com.example.myapplication;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public class TodoViewModel extends ViewModel {
    public MutableLiveData<Boolean> isHideDone = new MutableLiveData<>(false);
    public MutableLiveData<List<Todo>> todoList = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Todo>> doneList = new MutableLiveData<>(new ArrayList<>());
    private final ReentrantLock listLock = new ReentrantLock();

    public int findRealPosition(Todo todo,List<Todo> list){
        for(int i=0;i<list.size();i++){
            if (list.get(i).getId()==todo.getId()){
                return i;
            }
        }
        return -1;
    }
    public void updateTodoState(Todo todo, int adapterPosition, boolean newState, TodoAdapter adapter) {
        listLock.lock();
        try {
            List<Todo> currentTodoList = new ArrayList<>(todoList.getValue() != null ? todoList.getValue() : new ArrayList<>());
            List<Todo> currentDoneList = new ArrayList<>(doneList.getValue() != null ? doneList.getValue() : new ArrayList<>());
            boolean hideDone = isHideDone.getValue() != null && isHideDone.getValue();

            // 使用ID查找真实位置
            int realTodoPos = findRealPosition(todo, currentTodoList);
            int realDonePos = findRealPosition(todo, currentDoneList);

            todo.setCompleted(newState);

            if (newState) {
                // 从未完成移到已完成
                if (realTodoPos != -1) {
                    currentTodoList.remove(realTodoPos);
                    currentDoneList.add(0, todo); // 添加到已完成列表顶部

                    // 同步到静态列表
                    synchronized (TodoMain.LIST_LOCK) {
                        if (TodoMain.todoList.remove(todo)) {
                            TodoMain.doneList.add(0, todo);
                        }
                    }

                    if (hideDone) {
                        adapter.notifyItemRemoved(adapterPosition);
                    }
                }
            } else {
                // 从已完成移回未完成
                if (realDonePos != -1) {
                    currentDoneList.remove(realDonePos);
                    currentTodoList.add(0, todo); // 添加到未完成列表顶部

                    // 同步到静态列表
                    synchronized (TodoMain.LIST_LOCK) {
                        if (TodoMain.doneList.remove(todo)) {
                            TodoMain.todoList.add(0, todo);
                        }
                    }

                    if (!hideDone) {
                        adapter.notifyItemRemoved(adapterPosition);
                    }
                }
            }

            // 更新排序字段
            updateSortOrders(currentTodoList, 0);
            updateSortOrders(currentDoneList, 10000); // 给已完成事项一个较大的基础排序值

            todoList.postValue(currentTodoList);
            doneList.postValue(currentDoneList);

        } finally {
            listLock.unlock();
        }
    }

    private void updateSortOrders(List<Todo> list, int baseOrder) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSortOrder(baseOrder + i);
        }
    }

    /* public void syncWithGlobalList() {
        listLock.lock();
        try {
            todoList.postValue(new ArrayList<>(TodoMain.todoList));
            doneList.postValue(new ArrayList<>(TodoMain.doneList));
        } finally {
            listLock.unlock();
        }
    }*/




  /*  public void updateTodoState(Todo todo, int position, boolean newState, TodoAdapter adapter) {
        listLock.lock();
        try {
            List<Todo> currentTodoList = todoList.getValue();
            List<Todo> currentDoneList = doneList.getValue();
            Boolean hideDone = isHideDone.getValue();

            if (currentTodoList == null || currentDoneList == null || hideDone == null) return;
            TodoMain.todoList=new ArrayList<>(currentTodoList);
            TodoMain.doneList=new ArrayList<>(currentDoneList);
            if (hideDone && newState) {
                // 隐藏已办时，移到doneList并移除
                if (position >= 0 && position < currentTodoList.size()) {
                    currentTodoList.remove(position);
                    currentDoneList.add(todo);
                    adapter.notifyItemRemoved(position);
                }
            } else if (!hideDone && !newState) {
                // 显示所有时，从doneList移回
                if (currentDoneList.remove(todo)) {
                    currentTodoList.add(position, todo);
                    adapter.notifyItemInserted(position);
                }
            }

            // 通知LiveData更新（用postValue保证主线程）
            todoList.postValue(new ArrayList<>(currentTodoList));
            doneList.postValue(new ArrayList<>(currentDoneList));
            TodoMain.todoList=new ArrayList<>(currentTodoList);
            TodoMain.doneList=new ArrayList<>(currentDoneList);
        } finally {
            listLock.unlock();
        }
    }*/

    // 重置列表数据（用于页面刷新）
    public void resetLists(List<Todo> todos, List<Todo> dones) {
        listLock.lock();
        try {

            Collections.sort(todos, (t1, t2) -> Integer.compare(t1.getSortOrder(), t2.getSortOrder()));
            Collections.sort(dones, (t1, t2) -> Integer.compare(t1.getSortOrder(), t2.getSortOrder()));
            todoList.postValue(new ArrayList<>(todos));
            doneList.postValue(new ArrayList<>(dones));
        } finally {
            listLock.unlock();
        }
    }
}
/*
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TodoViewModel extends ViewModel {
    // 用 LiveData 包裹列表，数据变化时自动通知UI刷新
    public MutableLiveData<Boolean> isHideDone = new MutableLiveData<>(false);
    public MutableLiveData<List<Todo>> todoList = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Todo>> doneList = new MutableLiveData<>(new ArrayList<>());

    // 列表操作锁（保证线程安全）
    private final ReentrantLock listLock = new ReentrantLock();

    // 封装Todo状态变更的核心逻辑（替代你原来的零散操作）
    public void updateTodoState(Todo todo, int position, boolean newState, TodoAdapter adapter) {
        listLock.lock(); // 加锁
        try {
            List<Todo> currentTodoList = todoList.getValue();
            List<Todo> currentDoneList = doneList.getValue();
            boolean hideDone = isHideDone.getValue() != null ? isHideDone.getValue() : false;

            if (currentTodoList == null || currentDoneList == null) return;

            if (hideDone && newState) {
                // 移除待办，加入已完成
                currentTodoList.remove(position);
                currentDoneList.add(todo);
                adapter.notifyItemRemoved(position);
            } else if (!hideDone && !newState) {
                // 从已完成移除，插回待办
                if (currentDoneList.remove(todo)) {
                    currentTodoList.add(position, todo);
                    adapter.notifyItemInserted(position);
                }
            }

            // 通知LiveData数据变化（若需要全量刷新UI，可触发）
            todoList.postValue(currentTodoList);
            doneList.postValue(currentDoneList);
        } finally {
            listLock.unlock(); // 解锁
        }
    }
}*/