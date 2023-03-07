package com.acatapps.videomaker.adapter

import com.acatapps.videomaker.BuildConfig
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_sticker_list.view.*

class StickerListAdapter(val callback: (String) -> Unit) : BaseAdapter<String>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_sticker_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item =  mItemList[position]

        Glide.with(holder.itemView.context).load(BuildConfig.API_URL +item).fitCenter().into(view.previewSticker)

        view.setOnClickListener {
            callback.invoke(item)
        }
    }
}