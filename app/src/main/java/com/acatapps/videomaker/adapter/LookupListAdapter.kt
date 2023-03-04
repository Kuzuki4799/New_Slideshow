package com.acatapps.videomaker.adapter

import android.view.View
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.LookupDataModel
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.LookupUtils
import kotlinx.android.synthetic.main.item_lookup.view.*

class LookupListAdapter(val onSelectLookup:(LookupUtils.LookupType)->Unit): BaseAdapter<LookupDataModel>() {
    private var mCurrentPosition = -1
    init {
        val lookupDataList = LookupUtils.getLookupDataList()
        for(item in lookupDataList) {
            mItemList.add(LookupDataModel(item))
        }
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_lookup

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.setOnClickListener {
            onSelectLookup.invoke(item.lookupType)
            mCurrentPosition = position
            notifyDataSetChanged()
        }
        if(mCurrentPosition == position) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        view.imageThumb.setImageBitmap(BitmapUtils.getBitmapFromAsset("lut-preview/${item.lookupType}.jpg"))
        view.lookupNameLabel.text = item.name
    }

    fun highlightItem(lookupType: LookupUtils.LookupType) {
        for(index in 0 until  mItemList.size) {
            val item = mItemList[index]
            if(lookupType == item.lookupType) {
                mCurrentPosition = index
                notifyDataSetChanged()
                break
            }
        }
    }
}