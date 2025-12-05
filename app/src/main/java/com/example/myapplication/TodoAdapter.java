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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder>
        implements ItemTouchHelperAdapter {

    private List<Todo> todoList;
    private OnTodoClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnTodoClickListener {
        void onDeleteClick(int position);
        void onItemClick(int position);
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
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

        // 设置时间
        holder.timeTextView.setText(sdf.format(new Date(todo.getTimestamp())));

        // 设置完成状态的文本样式
        if (todo.isCompleted()) {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(0.5f);
        } else {
            holder.contentTextView.setPaintFlags(holder.contentTextView.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.contentTextView.setAlpha(1.0f);
        }

        // 点击事件
        holder.checkBox.setOnClickListener(v -> {
            todo.setCompleted(!todo.isCompleted());
            animateCompletion(holder);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
            animateCompletion(holder);
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
                notifyItemChanged(holder.getBindingAdapterPosition());
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
