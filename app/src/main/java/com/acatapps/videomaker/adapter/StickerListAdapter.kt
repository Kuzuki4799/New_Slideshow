package com.acatapps.videomaker.adapter

import android.net.Uri
import com.bumptech.glide.Glide
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_sticker_list.view.*

class StickerListAdapter(val callback:(String)->Unit) : BaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_sticker_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.previewSticker.setImageResource(item.toInt())
        view.setOnClickListener {
            callback.invoke(item)
        }
    }
}