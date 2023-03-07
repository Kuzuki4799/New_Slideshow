package com.hope_studio.video_maker_slideshow_maker.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.base.BaseViewHolder
import com.hope_studio.video_maker_slideshow_maker.models.MyStudioDataModel
import com.hope_studio.video_maker_slideshow_maker.utils.DimenUtils
import com.hope_studio.video_maker_slideshow_maker.utils.FileUtils
import com.hope_studio.video_maker_slideshow_maker.utils.MediaUtils
import com.hope_studio.video_maker_slideshow_maker.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_my_studio_in_home.view.*
import java.io.File

class MyStudioInHomeAdapter : BaseAdapter<MyStudioDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_my_studio_in_home

    var onClickItem: ((MyStudioDataModel) -> Unit)? = null

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        val size = DimenUtils.density(view.context) * 98
        view.imageThumb.clipToOutline = true

        view.txtTitle.text = File(item.filePath).name

        view.txtSize.text = FileUtils.formatFileSize(File(item.filePath).length())
        view.txtDate.text = FileUtils.convertToVideoDate(item.dateAdded)

        Glide.with(view.context).load(item.filePath).placeholder(R.drawable.ic_load_thumb).apply(
            RequestOptions().override(size.toInt())
        ).into(view.imageThumb)

        if (item.filePath.toLowerCase().contains(".mp4")) {
            view.grayBg.visibility = View.VISIBLE
            try {
                val duration = MediaUtils.getVideoDuration(item.filePath)
                view.durationLabel.text = Utils.convertSecToTimeString(duration / 1000)
            } catch (e: Exception) {
                File(item.filePath).delete()
                view.context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(item.filePath))
                    )
                )
            }
        } else {
            view.grayBg.visibility = View.GONE
            view.durationLabel.visibility = View.GONE
        }
        view.setOnClickListener {
            onClickItem?.invoke(item)
        }
    }

    override fun setItemList(arrayList: ArrayList<MyStudioDataModel>) {
        arrayList.sort()
        mItemList.clear()
        mItemList.addAll(arrayList)
        notifyDataSetChanged()
    }
}