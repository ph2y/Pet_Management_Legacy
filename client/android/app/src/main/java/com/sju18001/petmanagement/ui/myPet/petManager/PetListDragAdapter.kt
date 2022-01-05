package com.sju18001.petmanagement.ui.myPet.petManager

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R

class PetListDragAdapter(
    private val adapter: PetListAdapter
) : ItemTouchHelper.Callback() {
    public interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(itemViewHolder: PetListAdapter.HistoryListViewHolder)
        fun onRowClear(itemViewHolder: PetListAdapter.HistoryListViewHolder)
    }

    private val SCROLL_SENSITIVITY = 15

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is PetListAdapter.HistoryListViewHolder) {
                adapter.onRowSelected(viewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is PetListAdapter.HistoryListViewHolder) {
            adapter.onRowClear(viewHolder)
        }
    }

    // 스크롤 감도 조절
    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {
        return if(super.interpolateOutOfBoundsScroll(
                recyclerView,
                viewSize,
                viewSizeOutOfBounds,
                totalSize,
                msSinceStartScroll
            ) > 0) SCROLL_SENSITIVITY
        else -SCROLL_SENSITIVITY
    }

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return if(current.itemViewType == R.layout.create_pet_button ||
            target.itemViewType == R.layout.create_pet_button) false
        else super.canDropOver(recyclerView, current, target)
    }

    override fun isItemViewSwipeEnabled(): Boolean { return false }
    override fun isLongPressDragEnabled(): Boolean { return false }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}