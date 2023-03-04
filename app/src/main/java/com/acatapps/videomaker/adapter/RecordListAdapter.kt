package com.acatapps.videomaker.adapter

import android.view.View
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.RecordedDataModel
import kotlinx.android.synthetic.main.item_recorded.view.*

class RecordListAdapter : BaseAdapter<RecordedDataModel>() {

    var onSelect: ((RecordedDataModel) -> Unit)? = null

    override fun doGetViewType(position: Int): Int = R.layout.item_recorded

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        if (item.isSelect) {
            view.grayBg.visibility = View.VISIBLE
        } else {
            view.grayBg.visibility = View.GONE
        }

        view.setOnClickListener {
            setOffAll()
            item.isSelect = true
            onSelect?.invoke(item)
            notifyDataSetChanged()
        }
        view.recordName.text = "Record_${position}"
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.isSelect = false
        }
        notifyDataSetChanged()
    }
}