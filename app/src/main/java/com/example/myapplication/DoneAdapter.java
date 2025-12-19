package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DoneAdapter extends RecyclerView.Adapter<DoneAdapter.DoneViewHolder> {
    private List<Todo> doneList;
    private final OnRestoreClickListener onRestoreClickListener;
    private final SimpleDateFormat sdf=new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final DiffUtil.ItemCallback<Todo>TODO_ITEM_CALLBACK=new DiffUtil.ItemCallback<Todo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            //用Todo的唯一标识
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getContent().equals(newItem.getContent())
                    && oldItem.getTimestamp() == newItem.getTimestamp()
                    && oldItem.isCompleted() == newItem.isCompleted();
        }
    };
   public DoneAdapter(List<Todo> doneList,OnRestoreClickListener listener){
       this.doneList=doneList!=null?new ArrayList<>(doneList):new ArrayList<>();
       this.onRestoreClickListener=listener;
   }

   public void updateDoneList(List<Todo> newDoneList){
       List<Todo> oldList=new ArrayList<>(this.doneList);
       this.doneList=new ArrayList<>(newDoneList);
       //使用DiffUtil计算差异
       DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
           @Override
           public int getOldListSize() {
               return oldList.size();
           }

           @Override
           public int getNewListSize() {
               return doneList.size();
           }

           @Override
           public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
               return TODO_ITEM_CALLBACK.areItemsTheSame(
                       oldList.get(oldItemPosition),
                       doneList.get(newItemPosition)
               );
           }

           @Override
           public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
               return TODO_ITEM_CALLBACK.areContentsTheSame(
                       oldList.get(oldItemPosition),
                       doneList.get(newItemPosition)
               );
           }
       });
       diffResult.dispatchUpdatesTo(this); // 仅刷新变化的项
   }


   public void removeItem(long todoId) {

       int positionToRemove = -1;
       for (int i = 0; i < doneList.size(); i++) {
           if (doneList.get(i).getId() == todoId) {
               positionToRemove = i;
               break;
           }
       }

       if (positionToRemove != -1) {
           doneList.remove(positionToRemove);
           notifyItemRemoved(positionToRemove);
           // 通知后续项位置更新
           notifyItemRangeChanged(positionToRemove, doneList.size() - positionToRemove);
       } else {
           Log.d("DoneAdapter", "未找到ID为 " + todoId + " 的项");
       }
   }

   public interface OnRestoreClickListener{
       void onRestoreClick(Todo todo,int position);
   }

   @Override
    public DoneViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
       View view= LayoutInflater.from(parent.getContext())
               .inflate(R.layout.item_done,parent,false);
       return new DoneViewHolder(view);
   }

   @Override
    public void onBindViewHolder(@NonNull DoneViewHolder holder, int position){
       Todo todo=doneList.get(position);
       String content=todo.getContent()==null?"":todo.getContent();
       holder.tvTodoContent.setText(todo.getContent());
       holder.tvTime.setText(sdf.format(todo.getTimestamp()));
       holder.cbRestore.setChecked(false);
       holder.cbRestore.setOnClickListener(v->{
           int currentPos=holder.getBindingAdapterPosition();
           Log.d("DoneAdapter","点击位置:"+currentPos+",项内容:"+content);
           if(currentPos!=RecyclerView.NO_POSITION){
              onRestoreClickListener.onRestoreClick(todo,currentPos);
              //恢复后立即取消勾选,避免重复点击
              holder.cbRestore.setChecked(false);
          }
       });
   }

   @Override
    public int getItemCount(){
       return doneList==null?0:doneList.size();
   }

   public static class DoneViewHolder extends RecyclerView.ViewHolder{
       CheckBox cbRestore;
       TextView tvTodoContent;
       TextView tvTime;

       public DoneViewHolder(@NonNull View itemView){
           super(itemView);
           cbRestore=itemView.findViewById(R.id.checkbox2);
           tvTodoContent=itemView.findViewById(R.id.content);
           tvTime=itemView.findViewById(R.id.time);
       }
   }




}
