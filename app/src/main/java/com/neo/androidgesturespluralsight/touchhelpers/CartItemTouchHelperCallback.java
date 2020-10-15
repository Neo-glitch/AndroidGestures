package com.neo.androidgesturespluralsight.touchhelpers;


import android.graphics.Color;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.neo.androidgesturespluralsight.CartRecyclerViewAdapter;


/**
 * Item Touch Helper callBack class to help move and swipe items in rv
 */
public class CartItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;

    public CartItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }


    @Override
    public boolean isLongPressDragEnabled() {
        // enables rv list items to be dragged
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // enables list items to be Swipeable
        return true;
    }


    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {    // when item is let go or not in focus
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {   // when item is dragged
        super.onSelectedChanged(viewHolder, actionState);
        if(actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            viewHolder.itemView.setBackgroundColor(Color.RED);
        }
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // used to set the movement flags of the View Holder(layout item)
        if (viewHolder instanceof CartRecyclerViewAdapter.SectionHeaderViewHolder) {
            return 0;
        }
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // called when a list item is swiped
        mAdapter.onItemSwiped(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // called when an item is moved in the rv
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
}















