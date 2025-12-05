package com.example.myapplication;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private final Drawable deleteIcon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(ItemTouchHelperAdapter adapter, Drawable deleteIcon) {
        mAdapter = adapter;
        this.deleteIcon = deleteIcon;
        this.background = new ColorDrawable(Color.RED);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//上下拖拽调整位置
        int swipeFlags = ItemTouchHelper.START ;//只允许左滑控制删除
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getBindingAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
      //  int backgroundCornerOffset = 20;
        int iconWidth = deleteIcon.getIntrinsicWidth();
        int iconHeight = deleteIcon.getIntrinsicHeight();

        int iconMargin = (itemView.getHeight() - iconHeight) / 2;
        int iconTop = itemView.getTop() + iconMargin/ 2;
        int iconBottom = iconTop + iconHeight;
        int iconLeft = itemView.getRight() - iconMargin - iconWidth;
        int iconRight = itemView.getRight() - iconMargin;
        float backgroundScale = 1.5f;
        int backgroundWidth = (int) (iconWidth * backgroundScale);

         if (dX < 0) { // 向左滑动
             float swipeDistance = Math.abs(dX);
             float maxSwipeDistance = backgroundWidth;
             if (swipeDistance > maxSwipeDistance) {
                 swipeDistance = maxSwipeDistance;
             }
             int backgroundLeft = itemView.getRight() - (int)swipeDistance;
             int backgroundRight = itemView.getRight();
             int backgroundTop = itemView.getTop();
             int backgroundBottom = itemView.getBottom();
             background.setBounds(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom);
             background.draw(c);

             deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

             deleteIcon.draw(c);
             super.onChildDraw(c, recyclerView, viewHolder, -swipeDistance, dY, actionState, isCurrentlyActive);
             if ((backgroundRight - backgroundLeft) < backgroundWidth) {
                 backgroundLeft = backgroundRight - backgroundWidth;
             }
             background.setBounds(backgroundLeft, itemView.getTop(), backgroundRight, itemView.getBottom());
          //  background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                //    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // 不滑动
            background.setBounds(0, 0, 0, 0);
             deleteIcon.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        deleteIcon.draw(c);
    }
}