package com.hope_studio.video_maker_slideshow_maker.ho_adapter

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_application.VideoMakerApplication
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseViewHolder
import com.hope_studio.video_maker_slideshow_maker.ho_models.VideoForJoinDataModel
import com.hope_studio.video_maker_slideshow_maker.ho_utils.DimenUtils
import com.hope_studio.video_maker_slideshow_maker.ho_utils.MediaUtils
import com.hope_studio.video_maker_slideshow_maker.ho_utils.Utils
import kotlinx.android.synthetic.main.item_video_in_joiner.view.*
import kotlin.math.roundToInt

class VideoInJoinerAdapter : BaseAdapter<VideoForJoinDataModel>() {

    private val imageSize: Float = DimenUtils.density(VideoMakerApplication.getContext()) * 76

    override fun doGetViewType(position: Int): Int = R.layout.item_video_in_joiner

    var itemClick: ((VideoForJoinDataModel) -> Unit)? = null

    private var mCurrentPosition = -1

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = mItemList[position]
        val view = holder.itemView
        Glide.with(view.context).load(item.path).apply(RequestOptions().override(imageSize.toInt()))
            .into(view.mediaThumb)
        view.durationLabel.text = Utils.convertSecToTimeString(
            (MediaUtils.getVideoDuration(item.path).toFloat() / 1000).roundToInt()
        )

        if (item.select) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }

        view.setOnClickListener {
            if (mCurrentPosition >= 0) mItemList[mCurrentPosition].select = false
            mCurrentPosition = position
            mItemList[mCurrentPosition].select = true
            notifyDataSetChanged()
            itemClick?.invoke(item)
        }

    }

    fun highlightItem(id: Int) {
        for (item in mItemList) {
            item.select = item.id == id
        }
        notifyDataSetChanged()
    }
}