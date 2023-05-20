package com.hope_studio.video_maker_slideshow_maker.ho_adapter

import android.graphics.Color
import android.view.View
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseViewHolder
import kotlinx.android.synthetic.main.item_color_list.view.*
import java.lang.Exception

class ColorListAdapter(val callback: (Int) -> Unit) : BaseAdapter<String>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_color_list

    private var mColorSelected = ""

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        try {
            view.colorPreview.setBackgroundColor(Color.parseColor(item))
            view.setOnClickListener {
                mColorSelected = item
                callback.invoke(Color.parseColor(item))
                notifyDataSetChanged()
            }
            if (mColorSelected == item) {
                view.translationY = -20f
                view.strokeInColor.visibility = View.VISIBLE
            } else {
                view.translationY = 0f
                view.strokeInColor.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}