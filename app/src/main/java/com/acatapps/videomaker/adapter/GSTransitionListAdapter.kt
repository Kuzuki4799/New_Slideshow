package com.acatapps.videomaker.adapter

import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.GSTransitionDataModel
import com.acatapps.videomaker.slide_show_transition.GSTransitionUtils
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import kotlinx.android.synthetic.main.item_gs_transition_list.view.*

class GSTransitionListAdapter(private val onSelectTransition:(GSTransitionDataModel)->Unit) : BaseAdapter<GSTransitionDataModel>() {

    init {
        addGSTransitionData(GSTransitionUtils.getGSTransitionList())
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]

        view.transitionNameLabel.text = item.gsTransition.transitionName
        if(item.selected) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        view.setOnClickListener {
            highlightItem(item.gsTransition)
            onSelectTransition.invoke(item)
        }
        Glide.with(view.context)
            .load(Uri.parse("file:///android_asset/transition-preview/${item.gsTransition.transitionName}.jpg"))
            .into(view.imagePreview)
    }

    fun addGSTransitionData(gsTransitionList:ArrayList<GSTransition>) {
        mItemList.clear()
        notifyDataSetChanged()
        for(gsTransition in gsTransitionList) {
            mItemList.add(GSTransitionDataModel(gsTransition))
        }
        notifyDataSetChanged()
    }

    fun highlightItem(gsTransition: GSTransition) {
        for(item in mItemList) {
            item.selected = item.gsTransition.transitionCodeId == gsTransition.transitionCodeId
        }
        notifyDataSetChanged()
    }

}