package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// 继承RecyclerView.Adapter，泛型是自定义的ViewHolder
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private Context context;       // 上下文（用于加载布局）
    private List<Note> noteList;   // 笔记数据列表
    // 时间格式化器：将毫秒转成“MM-dd, HH:mm”格式
    private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd, HH:mm", Locale.getDefault());

    // 构造函数：接收上下文和数据列表
    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    // 创建ViewHolder：加载列表项布局
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载item_note布局，传入父容器（RecyclerView）
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView); // 返回ViewHolder实例
    }

    // 绑定数据：将数据设置到ViewHolder的控件上
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position); // 获取当前位置的笔记
        holder.tvTitle.setText(note.getTitle()); // 设置标题
        holder.tvContent.setText(note.getContent()); // 设置内容
        // 设置修改时间（拼接“Modified at”前缀）
        holder.tvTime.setText("Modified at " + sdf.format(note.getModifiedTime()));
    }

    // 获取列表项数量
    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    // 刷新数据：当数据库数据变化时，更新适配器的列表并刷新UI
    public void refreshNotes(List<Note> newNotes) {
        this.noteList = newNotes;
        notifyDataSetChanged(); // 通知RecyclerView重新绑定数据
    }

    // 自定义ViewHolder：持有列表项的所有控件
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;   // 笔记标题
        TextView tvContent; // 笔记内容
        TextView tvTime;    // 修改时间

        // 构造函数：找到列表项中的控件
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
