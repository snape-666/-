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

    private  List<Todo> todoList;
    private final OnTodoClickListener listener;
    private final TodoViewModel todoViewModel;

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat deadlineSdf =new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault());

    private RecyclerView recyclerView;
    public interface OnTodoClickListener {
        void onDeleteClick(int position);
        void onItemClick(int position);
        void onCheckboxClick(int position,boolean newState);
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener, TodoViewModel todoViewModel) {
        this.todoList = todoList;
        this.listener = listener;
        this.todoViewModel = todoViewModel;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView=recyclerView;
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
        //展示描述和截止日期
        if (todo.getDescription()!=null&&!todo.getDescription().isEmpty()){
            holder.descTextView.setVisibility(View.VISIBLE);
            holder.descTextView.setText(todo.getDescription());
        }
        else {
            holder.descTextView.setVisibility(View.GONE);
        }
        if(todo.getDeadline()>0){
            holder.deadlineTextView.setVisibility(View.VISIBLE);
            holder.deadlineTextView.setText("截止:"+deadlineSdf.format(new Date(todo.getDeadline())));
        }
        else {
            holder.deadlineTextView.setVisibility(View.GONE);
        }
        //使用标签存储id,避免位置错乱
        holder.itemView.setTag(todo.getId());
        holder.checkBox.setTag(todo.getId());
        holder.deleteButton.setTag(todo.getId());

        // 设置完成状态样式
        if (todo.isCompleted()) {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(0.5f);
            holder.descTextView.setAlpha(0.5f);
            holder.deadlineTextView.setAlpha(0.5f);
        } else {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(1.0f);
            holder.descTextView.setAlpha(0.5f);
            holder.deadlineTextView.setAlpha(0.5f);
        }

            holder.checkBox.setOnClickListener(v -> {
                        long todoId = (Long) v.getTag();
                        Todo clickedTodo = findTodoById(todoId);
                        if (clickedTodo == null) return;
                        boolean newState = !clickedTodo.isCompleted();
                        int currentPosition=holder.getBindingAdapterPosition();
                        if(currentPosition!=RecyclerView.NO_POSITION){
                            listener.onCheckboxClick(currentPosition,newState);
                        }

                        animateCompletion(holder);
                        notifyItemChanged(currentPosition);

                     holder.itemView.post(() -> {
                         boolean isHideDone=todoViewModel.isHideDone!=null&&todoViewModel.isHideDone.getValue()!=null&&todoViewModel.isHideDone.getValue();
                            if (newState && todoViewModel.isHideDone.getValue() != null && todoViewModel.isHideDone.getValue()) {
                                todoList.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                            } else {
                                notifyItemChanged(currentPosition);
                            }
                        });
            });




        holder.deleteButton.setOnClickListener(v -> {
            long todoId=(Long)v.getTag();
            Todo clickedTodo = findTodoById(todoId);
            if (clickedTodo == null) return;
            int currentPosition = holder.getBindingAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(currentPosition);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            long todoId=(Long)v.getTag();
            Todo clickedTodo = findTodoById(todoId);
            if (clickedTodo == null) return;
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
        notifyItemMoved(fromPosition, toPosition);
        updateSortOrdersInDatabase();
    }

    private void updateSortOrdersInDatabase(){
        if (todoList.isEmpty())return;
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

    public void setNewTodoList(List<Todo>newTodoList){
        this.todoList=newTodoList;
        notifyDataSetChanged();
    }
    public void updateTodoList(List<Todo> newTodoList){
        setNewTodoList(newTodoList);
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView contentTextView;
        TextView descTextView;
        TextView deadlineTextView;
        TextView timeTextView;
        ImageButton deleteButton;

        public TodoViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            contentTextView = itemView.findViewById(R.id.content);
            descTextView=itemView.findViewById(R.id.desc);
            deadlineTextView=itemView.findViewById(R.id.deadline);
            timeTextView = itemView.findViewById(R.id.time);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
