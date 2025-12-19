package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private Context context;
    private List<Note> noteList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd, HH:mm", Locale.getDefault());

    OnNoteDeleteListener deleteListener;

    public interface OnNoteDeleteListener{
        void onDeleteNote(int noteId);
    }

    public NoteAdapter(Context context, List<Note> noteList,OnNoteDeleteListener deleteListener) {
        this.context = context;
        this.noteList = noteList;
        this.deleteListener=deleteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.tvTime.setText("Modified at " + sdf.format(note.getModifiedTime()));
        holder.itemView.setOnClickListener(v->{
            Intent intent=new Intent(context,AddNoteActivity.class);
            intent.putExtra("NOTE_ID",note.getId());
            intent.putExtra("NOTE_TITLE",note.getTitle());
            intent.putExtra("NOTE_CONTENT",note.getContent());
            intent.putExtra("NOTE_TIME",note.getModifiedTime());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    public void refreshNotes(List<Note> newNotes) {
        this.noteList = newNotes;
        notifyDataSetChanged();
    }

    public void removeNoteById(int noteId) {
        if (noteList == null) return;
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getId() == noteId) {
                noteList.remove(i);
                notifyItemRemoved(i); // 带动画的删除
                notifyItemRangeChanged(i, noteList.size()); // 刷新后续项的位置
                break;
            }
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
