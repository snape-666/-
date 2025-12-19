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


            int realTodoPos = findRealPosition(todo, currentTodoList);
            int realDonePos = findRealPosition(todo, currentDoneList);

            todo.setCompleted(newState);

            if (newState) {
                if (realTodoPos != -1) {
                    currentTodoList.remove(realTodoPos);
                    currentDoneList.add(0, todo);

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
                if (realDonePos != -1) {
                    currentDoneList.remove(realDonePos);
                    currentTodoList.add(0, todo);
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

            updateSortOrders(currentTodoList, 0);
            updateSortOrders(currentDoneList, 10000);

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
