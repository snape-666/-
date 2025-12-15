package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder>
        implements ItemTouchHelperAdapter {

    private final List<Todo> todoList;
    private final OnTodoClickListener listener;
    private final TodoViewModel todoViewModel;

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private RecyclerView recyclerView;
    public interface OnTodoClickListener {
        void onDeleteClick(int position);
        void onItemClick(int position);
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener, TodoViewModel todoViewModel) {
        this.todoList = todoList;
        this.listener = listener;
        this.todoViewModel = todoViewModel;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.checkBox.setChecked(todo.isCompleted());
        holder.contentTextView.setText(todo.getContent());
        holder.timeTextView.setText(sdf.format(new Date(todo.getTimestamp())));
        //使用标签存储id,避免位置错乱
        holder.itemView.setTag(todo.getId());
        holder.checkBox.setTag(todo.getId());
        holder.deleteButton.setTag(todo.getId());

        // 设置完成状态样式
        if (todo.isCompleted()) {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(0.5f);
        } else {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(1.0f);
        }

        // ===== 修复点：CheckBox点击逻辑（同步数据库+列表）=====
        holder.checkBox.setOnClickListener(v -> {
            long todoId=(Long)v.getTag();
            Todo clickedTodo=findTodoById(todoId);
            if (clickedTodo==null)return;
            boolean newState = !clickedTodo.isCompleted();
            clickedTodo.setCompleted(newState);
            // 后台更新数据库（原代码直接在主线程操作数据库）
           int realPosition=-1;
           for (int i=0;i<todoList.size();i++){
               if (todoList.get(i).getId()==todoId){
                   realPosition=i;
                   break;
               }
           }
           if (realPosition==-1)return;

            new Thread(() -> {
                TodoDatabase.getInstance(holder.itemView.getContext())
                        .getTodoDao().updateTodo(todo);}).start();
            animateCompletion(holder);

            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            // 更新ViewModel并刷新列表
            todoViewModel.updateTodoState(todo, currentPosition, newState, this);
            notifyItemChanged(currentPosition); // 局部刷新，避免全量刷新

        holder.itemView.post(() -> {
            if (newState && todoViewModel.isHideDone.getValue() != null
                    && todoViewModel.isHideDone.getValue()) {
                // 如果隐藏已办且标记为已完成，移除该项
                todoList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            } else {
                notifyItemChanged(currentPosition);
            }
        });
    });




        holder.deleteButton.setOnClickListener(v -> {
            long todoId=(Long)v.getTag();
            int positionToDelete=-1;
            for (int i=0;i<todoList.size();i++){
                if (todoList.get(i).getId()==todoId){
                    positionToDelete=i;
                    break;
                }
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(currentPosition);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            long todoId=(Long)v.getTag();
            int positionToClick = -1;
            for (int i = 0; i < todoList.size(); i++) {
                if (todoList.get(i).getId() == todoId) {
                    positionToClick = i;
                    break;
                }
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(currentPosition);
            }
        });
    }

    private Todo findTodoById(long id){
        for (Todo todo:todoList){
            if (todo.getId()==id){
                return todo;
            }
        }
        return null;
    }
    private void animateCompletion(TodoViewHolder holder) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            holder.checkBox.setScaleX(1 + value * 0.1f); // 缩小动画缩放比例，避免变形
            holder.checkBox.setScaleY(1 + value * 0.1f);
            holder.checkBox.setAlpha(1 - value * 0.2f);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.checkBox.setScaleX(1f);
                holder.checkBox.setScaleY(1f);
                holder.checkBox.setAlpha(1f);
            }
        });
        animator.start();
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(todoList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(todoList, i, i - 1);
            }
        }
        updateSortOrdersInDatabase();
        notifyItemMoved(fromPosition, toPosition);
    }

    private void updateSortOrdersInDatabase(){
        new Thread(() -> {
            TodoDao todoDao = TodoDatabase.getInstance(recyclerView.getContext()).getTodoDao();
            for (int i = 0; i < todoList.size(); i++) {
                Todo todo = todoList.get(i);
                todo.setSortOrder(i);
                todoDao.updateTodo(todo);
            }
        }).start();
    }
    @Override
    public void onItemDismiss(int position) {
        if(position!=RecyclerView.NO_POSITION){
            listener.onDeleteClick(position);
        }
    }

    // ===== 修复点：updateTodoList移除Handler，直接主线程更新（避免延迟）=====
    public void updateTodoList(List<Todo> newTodoList) {
        this.todoList.clear();
        this.todoList.addAll(newTodoList);
        notifyDataSetChanged(); // 若要优化，可改用DiffUtil
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView contentTextView;
        TextView timeTextView;
        ImageButton deleteButton;

        public TodoViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            contentTextView = itemView.findViewById(R.id.content);
            timeTextView = itemView.findViewById(R.id.time);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
/*
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder>
        implements ItemTouchHelperAdapter {

    private List<Todo> todoList;//数据源
    private OnTodoClickListener listener;
    private TodoViewModel todoViewModel;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());//时间格式化

    public interface OnTodoClickListener {
        void onDeleteClick(int position);//删除按钮点击

        void onItemClick(int position);//整个项目点击
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener,TodoViewModel todoViewModel) {
        this.todoList = todoList;
        this.listener = listener;
        this.todoViewModel=todoViewModel;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())//获取上下文,解析列表项布局
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.checkBox.setChecked(todo.isCompleted());//对应当前操作的列表项
        holder.contentTextView.setText(todo.getContent());

        // 设置时间
        holder.timeTextView.setText(sdf.format(new Date(todo.getTimestamp())));

        // 设置完成状态的文本样式
        if (todo.isCompleted()) {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);//添加删除线
            holder.contentTextView.setAlpha(0.5f);//半透明
        } else {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(1.0f);
        }

        // 点击事件
        //处理todo,并添加到已办
        holder.checkBox.setOnClickListener(v -> {
            boolean newState=!todo.isCompleted();
            todo.setCompleted(newState);
            //更新数据库
            TodoDatabase.getInstance(holder.itemView.getContext()).getTodoDao().updateTodo(todo);
            animateCompletion(holder);//执行动画

            int currentposition=holder.getBindingAdapterPosition();
            if(currentposition==RecyclerView.NO_POSITION){
                return;
            }
            todoViewModel.updateTodoState(todo,position,newState,this);
            //列表管理
     /*    synchronized (TodoMain.LIST_LOCK) {
             if (TodoMain.isHideDone && newState) {
                 TodoMain.todoList.remove(position);//getBinding..获取当前项在adapter中的绑定位置
                 TodoMain.doneList.add(todo);//todo为当前操作的实体对象(当前列表项对应的数据)
                 notifyItemRemoved(position);
             } else if (!TodoMain.isHideDone && !newState) {//newState新状态
                 if (TodoMain.doneList.remove(todo)) {
                     todoList.remove(position);
                     TodoMain.todoList.add(position, todo);
                     //局部刷新列表
                     notifyItemInserted(position);
                 }
             }
         }*/
 /*       });
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
            //animateCompletion(holder);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    // 完成动画
    private void animateCompletion(TodoViewHolder holder) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            holder.checkBox.setScaleX(1 + value * 0.5f);
            holder.checkBox.setScaleY(1 + value * 0.5f);
            holder.checkBox.setAlpha(1 - value * 0.5f);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.checkBox.setScaleX(1f);
                holder.checkBox.setScaleY(1f);
                holder.checkBox.setAlpha(1f);

            }
        });
        animator.start();
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    // 拖拽相关方法
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(todoList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(todoList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateTodoList(List<Todo> newTodoList) {
        this.todoList.clear();
        this.todoList.addAll(newTodoList);
       new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run(){
                notifyDataSetChanged();
            }
        });
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView contentTextView;
        TextView timeTextView;
        ImageButton deleteButton;

        public TodoViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            contentTextView = itemView.findViewById(R.id.content);
            timeTextView = itemView.findViewById(R.id.time);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}*/
