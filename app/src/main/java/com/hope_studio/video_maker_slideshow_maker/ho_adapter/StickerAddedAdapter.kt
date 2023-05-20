package com.hope_studio.video_maker_slideshow_maker.ho_adapter

import android.view.View
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseViewHolder
import com.hope_studio.video_maker_slideshow_maker.models.StickerAddedDataModel
import com.hope_studio.video_maker_slideshow_maker.ho_utils.Logger
import kotlinx.android.synthetic.main.item_sticker_added.view.*

class StickerAddedAdapter(private val onChange: OnChange) : BaseAdapter<StickerAddedDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_sticker_added

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.setOnClickListener {
            setOffAll()
            mCurrentItem?.inEdit = false
            item.inEdit = true
            mCurrentItem = item
            notifyDataSetChanged()
            onChange.onClickSticker(item)
        }
        Logger.e("start end --> ${item.startTimeMilSec} ${item.endTimeMilSec}")
        if (item.inEdit) {
            view.grayBg.visibility = View.VISIBLE
        } else {
            view.grayBg.visibility = View.GONE
        }
        view.stickerAddedPreview.setImageBitmap(item.bitmap)
    }

    fun addNewSticker(stickerAddedDataModel: StickerAddedDataModel) {
        mCurrentItem?.inEdit = false
        mItemList.add(stickerAddedDataModel)
        mCurrentItem = stickerAddedDataModel
        notifyDataSetChanged()
    }

    fun deleteItem(stickerAddedDataModel: StickerAddedDataModel) {
        mItemList.remove(stickerAddedDataModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.inEdit = false
        }
        notifyDataSetChanged()
    }

    interface OnChange {
        fun onClickSticker(stickerAddedDataModel: StickerAddedDataModel)
    }
}