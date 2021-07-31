package com.sju18001.petmanagement.ui.myPet.petManager

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class PetListDragAdapter(val adapter: PetListAdapter) : ItemTouchHelper.Callback() {

    public interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(itemViewHolder: PetListAdapter.HistoryListViewHolder)
        fun onRowClear(itemViewHolder: PetListAdapter.HistoryListViewHolder)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
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

    override fun isItemViewSwipeEnabled(): Boolean { return false }
    override fun isLongPressDragEnabled(): Boolean { return false }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}