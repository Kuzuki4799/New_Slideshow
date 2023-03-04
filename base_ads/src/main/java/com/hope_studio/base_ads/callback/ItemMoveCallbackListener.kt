package com.hope_studio.base_ads.callback

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hope_studio.base_ads.base.adapter.BaseArrangeAdapter
import com.hope_studio.base_ads.base.adapter.holder.BaseHolder

class ItemMoveCallbackListener(
    private val isFull: Boolean, val adapter: BaseArrangeAdapter<*>
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags: Int = if (isFull) {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
        } else {
            ItemTouchHelper.START or ItemTouchHelper.END
        }
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            adapter.onRowSelected(viewHolder as BaseHolder?)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        adapter.onRowClear(viewHolder as BaseHolder)
    }

    interface ArrangeListener {

        fun onRowMoved(fromPosition: Int, toPosition: Int)

        fun onRowSelected(itemViewHolder: BaseHolder?)

        fun onRowClear(itemViewHolder: BaseHolder?)
    }
}