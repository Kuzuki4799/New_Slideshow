package com.hope_studio.video_maker_slideshow_maker.ho_adapter

import android.view.View
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseViewHolder
import com.hope_studio.video_maker_slideshow_maker.models.TextStickerAddedDataModel
import kotlinx.android.synthetic.main.item_text_sticker_added.view.*

class TextStickerAddedAdapter(private val onChange: OnChange) :
    BaseAdapter<TextStickerAddedDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_text_sticker_added

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]

        view.setOnClickListener {
            setOffAll()
            mCurrentItem?.inEdit = false
            item.inEdit = true
            mCurrentItem = item
            notifyDataSetChanged()
            onChange.onClickTextSticker(item)
        }

        if (item.inEdit) {
            view.grayBg.visibility = View.VISIBLE
        } else {
            view.grayBg.visibility = View.GONE
        }
        view.textContent.text = item.text
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.inEdit = false
        }
        mCurrentItem = null
        notifyDataSetChanged()
    }

    fun addNewText(textStickerAddedDataModel: TextStickerAddedDataModel) {
        mCurrentItem?.inEdit = false
        mItemList.add(textStickerAddedDataModel)
        mCurrentItem = textStickerAddedDataModel
        notifyDataSetChanged()
    }

    fun deleteItem(textStickerAddedDataModel: TextStickerAddedDataModel) {
        mItemList.remove(textStickerAddedDataModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    fun getItemBytViewId(viewId: Int): TextStickerAddedDataModel? {
        for (item in mItemList) {
            if (item.viewId == viewId) {
                return item
            }
        }
        return null
    }

    interface OnChange {
        fun onClickTextSticker(textStickerAddedDataModel: TextStickerAddedDataModel)
    }
}