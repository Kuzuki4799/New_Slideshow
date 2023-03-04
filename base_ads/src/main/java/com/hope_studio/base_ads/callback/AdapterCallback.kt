package com.hope_studio.base_ads.callback

import android.view.View
import com.hope_studio.base_ads.base.adapter.holder.BaseHolder

class AdapterCallback {

    interface OnStartDragListener {
        fun onStartDrag(holder: BaseHolder)
    }

    interface OnItemClickListener<T> {
        fun onItemClickClicked(value: T)
    }

    interface OnItemViewClickListener<T> {
        fun onItemClickClicked(view: View, value: T, position: Int)
    }

    interface OnItemClickPositionListener<T> {
        fun onItemClickClicked(value: T, position: Int)
    }

    interface OnItemClickArrayListener<T> {
        fun onItemClickClicked(value: ArrayList<T>)
    }

    interface OnItemViewClickArrayListener<T> {
        fun onItemClickClicked(view: View, value: ArrayList<T>, position: Int)
    }

    interface OnItemClickPositionArrayListener<T> {
        fun onItemClickClicked(value: ArrayList<T>, position: Int)
    }

    interface OnItemDoubleClickListener<T> {
        fun onItemClickClicked(value: T)

        fun onItemTwoClickClicked(value: T)
    }

    interface OnItemDoublePositionClickListener {
        fun onItemClickClicked(position: Int)

        fun onItemTwoClickClicked(position: Int)
    }

    interface OnItemClickLongListener<T> {
        fun onItemClickClicked(value: T)

        fun onItemLongClicked(value: T)
    }

    interface OnItemClickLongPositionListener {
        fun onItemClickClicked(position: Int)

        fun onItemLongClicked(position: Int)
    }

    interface OnItemLongClickListener<T> {
        fun onItemLongClicked(value: T)
    }

    interface OnItemPositionClickListener {
        fun onItemClickClicked(position: Int)
    }

    interface OnItemPositionLongClickListener {
        fun onItemLongClicked(position: Int)
    }

    interface OnClickItemAdapterListener<T> {
        fun onItemClick(position: Int, item: T)
    }
}