package com.acatapps.videomaker.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.ThemeDataModel
import com.acatapps.videomaker.slide_show_theme.ThemeUtils
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.utils.Logger
import kotlinx.android.synthetic.main.item_slide_theme.view.*
import javax.security.auth.callback.Callback

class SlideThemeListAdapter(val callback: (ThemeData)->Unit) : BaseAdapter<ThemeDataModel>() {

    init {
       getAllThemeOnDevice()
    }


    override fun doGetViewType(position: Int): Int = R.layout.item_slide_theme

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.setOnClickListener {
            callback.invoke(item.themeData)
            highlightItem(item.themeData.themeVideoFilePath)
        }
        view.themeNameLabel.text = item.name
        if(item.name == "none") {
            view.themeIcon.setImageResource(R.drawable.ic_none)
        } else {
            Glide.with(view.context).load(item.videoPath).apply(RequestOptions().override(200)).into(view.themeIcon)
        }
        if(item.selected) {
            view.strokeBg.visibility = View.VISIBLE
            view.blackBgOfTitleView.setBackgroundColor(VideoMakerApplication.getContext().resources.getColor(R.color.orangeA02))
        } else {
            view.strokeBg.visibility = View.GONE
            view.blackBgOfTitleView.setBackgroundColor(VideoMakerApplication.getContext().resources.getColor(R.color.blackAlpha45))
        }


    }

    fun highlightItem(path:String) {
        for(item in mItemList) {
            item.selected = item.themeData.themeVideoFilePath == path
        }
        notifyDataSetChanged()
    }

    private fun getAllThemeOnDevice() {
        for(themeData in ThemeUtils.getThemeDataList()) {
            mItemList.add(ThemeDataModel(themeData))
        }
        notifyDataSetChanged()
    }

}