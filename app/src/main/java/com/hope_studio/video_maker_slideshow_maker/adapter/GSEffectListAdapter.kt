package com.hope_studio.video_maker_slideshow_maker.adapter

import android.view.View
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.base.BaseViewHolder
import com.hope_studio.video_maker_slideshow_maker.gs_effect.GSEffectUtils
import com.hope_studio.video_maker_slideshow_maker.models.GSEffectDataModel
import com.hope_studio.video_maker_slideshow_maker.utils.BitmapUtils
import kotlinx.android.synthetic.main.item_gs_transition_list.view.*

class GSEffectListAdapter : BaseAdapter<GSEffectDataModel>() {

    var onSelectEffectCallback: ((Int, GSEffectUtils.EffectType) -> Unit)? = null

    init {
        val effectDataList = GSEffectUtils.getAllGSEffectData()
        for (item in effectDataList) {
            mItemList.add(GSEffectDataModel(item).apply {
                if (item.effectType == GSEffectUtils.EffectType.NONE) {
                    isSelect = true
                }
            })
        }
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = mItemList[position]
        val view = holder.itemView
        view.transitionNameLabel.text = item.name
        if (item.isSelect) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        view.setOnClickListener {
            setOffAll()
            item.isSelect = true
            notifyDataSetChanged()
            onSelectEffectCallback?.invoke(position, item.gsEffectData.effectType)
        }

        view.imagePreview.setImageBitmap(BitmapUtils.getBitmapFromAsset("effect-preview/${item.gsEffectData.effectType}.jpg"))
    }

    private fun setOffAll() {
        for (item in mItemList) {
            item.isSelect = false
        }
    }

    fun selectEffect(effectType: GSEffectUtils.EffectType) {
        setOffAll()
        for (item in mItemList) {
            if (item.gsEffectData.effectType == effectType) {
                item.isSelect = true
                notifyDataSetChanged()
                return
            }
        }
    }
}