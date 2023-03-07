package com.hope_studio.video_maker_slideshow_maker.adapter

import com.hope_studio.video_maker_slideshow_maker.BuildConfig
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.base.BaseViewHolder
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