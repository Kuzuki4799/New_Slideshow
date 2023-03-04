package com.acatapps.videomaker.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideData
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.LookupUtils
import kotlinx.android.synthetic.main.item_image_list_in_slide_show.view.*

class ImageWithLookupAdapter(private val onSelectImage:(Long)->Unit): BaseAdapter<ImageSlideData>() {
    private var mCurrentPositon = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_image_list_in_slide_show

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.setOnClickListener {
            mCurrentItem = item
            mCurrentPositon = position
            onSelectImage.invoke(item.slideId)
            notifyDataSetChanged()
        }
        if(position == mCurrentPositon) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        Glide.with(view.context).load(item.fromImagePath).apply(RequestOptions().override((DimenUtils.density(view.context)*64).toInt())).into(view.imagePreview)
    }

    fun changeLookupOfCurretItem(lookupType: LookupUtils.LookupType) {
        mCurrentItem?.lookupType = lookupType
    }
    fun changeHighlightItem(position:Int) :LookupUtils.LookupType{
        if(position >= 0 && position < mItemList.size) {
            mCurrentPositon = position
            mCurrentItem = mItemList[mCurrentPositon]
            notifyDataSetChanged()
            return mItemList[mCurrentPositon].lookupType
        }
        return LookupUtils.LookupType.NONE

    }

}