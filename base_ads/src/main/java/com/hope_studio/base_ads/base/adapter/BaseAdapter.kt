package com.hope_studio.base_ads.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hope_studio.base_ads.base.adapter.holder.BaseHolder
import java.util.*

abstract class BaseAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list = ArrayList<T>()

    protected abstract fun getLayoutId(): Int

    protected abstract fun bindData(holder: BaseHolder, item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BaseHolder(
            LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val t = list[position]
        bindData(viewHolder as BaseHolder, t, position)
    }

    fun addSingleItem(item: T?) {
        if (item != null) {
            list.add(item)
            notifyItemInserted(list.size - 1)
            notifyItemRangeInserted(list.size - 1, list.size)
        }
    }

    fun addSingleItemAtSpecificPosition(item: T?, position: Int) {
        if (item != null) {
            list.add(position, item)
            notifyItemInserted(position)
        }
    }

    fun addArrayList(items: List<T>?) {
        if (items != null) {
            list.addAll(items)
            notifyDataSetChanged()
        }
    }

    fun replaceArrayList(items: List<T>?) {
        if (items != null) {
            list.clear()
            addArrayList(items)
        }
    }

    fun addArray(items: Array<T>?) {
        if (items != null) {
            addArrayList(listOf(*items))
            notifyDataSetChanged()
        }
    }

    fun addOrUpdateSingleItem(item: T?) {
        if (item != null) {
            val i = list.indexOf(item)
            if (i >= 0) {
                list[i] = item
                notifyItemChanged(i)
            } else {
                addSingleItem(item)
            }
        }
    }

    fun addOrUpdateArrayList(items: List<T>?) {
        if (items != null) {
            for (item in items) {
                addOrUpdateSingleItem(item)
            }
            notifyDataSetChanged()
        }
    }

    open fun addAllAtPosition(position: Int, listItems: ArrayList<T>) {
        list.addAll(position, listItems)
        notifyDataSetChanged()
    }

    fun getSingleItemUsingPosition(position: Int): T {
        return list[position]
    }

    fun removeSingleItemUsingPosition(position: Int) {
        if (position >= 0 && position < list.size) {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size)
        }
    }

    fun removeSingleItem(item: T?) {
        if (item != null) {
            val position = list.indexOf(item)
            removeSingleItemUsingPosition(position)
        }
    }

    fun clearAllItem() {
        list.clear()
        notifyDataSetChanged()
    }

    private fun getDataByPosition(position: Int): T? {
        var t: T? = null
        if (position != RecyclerView.NO_POSITION) {
            t = list[position]
        }
        return t
    }

    override fun getItemCount(): Int {
        return if (list != null) {
            list.size
        } else 0
    }
}