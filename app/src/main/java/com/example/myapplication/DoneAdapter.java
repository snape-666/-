package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoneAdapter extends RecyclerView.Adapter<DoneAdapter.DoneViewHolder> {
    private List<Todo> doneList;
    private OnRestoreClickListener onRestoreClickListener;
   public DoneAdapter(List<Todo> doneList,OnRestoreClickListener listener){
       this.doneList=doneList;
       this.onRestoreClickListener=listener;
   }

   public void updateDoneList(List<Todo> newDoneList){
       this.doneList=newDoneList;
       notifyDataSetChanged();
   }
   public interface OnRestoreClickListener{
       void onRestoreClick(Todo item);
   }

   @Override
    public DoneViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_done,parent,false);
       return new DoneViewHolder(view);
   }

   @Override
    public void onBindViewHolder(DoneViewHolder holder,int position){
       Todo item=doneList.get(position);
       holder.tvTodoContent.setText(item.getContent());
       holder.cbRestore.setChecked(false);
       holder.cbRestore.setOnClickListener(v->{
          if(holder.cbRestore.isChecked()){
              onRestoreClickListener.onRestoreClick(item);
          }
       });
   }

   @Override
    public int getItemCount(){
       return doneList.size();
   }

   public static class DoneViewHolder extends RecyclerView.ViewHolder{
       CheckBox cbRestore;
       TextView tvTodoContent;

       public DoneViewHolder(View itemView){
           super(itemView);
           cbRestore=itemView.findViewById(R.id.checkbox2);
           tvTodoContent=itemView.findViewById(R.id.content);
       }
   }




}
