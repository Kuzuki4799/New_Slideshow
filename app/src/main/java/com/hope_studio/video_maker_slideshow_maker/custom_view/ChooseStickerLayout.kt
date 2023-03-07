package com.hope_studio.video_maker_slideshow_maker.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.hope_studio.video_maker_slideshow_maker.BuildConfig
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope.video.StickerFileAsset
import com.hope_studio.video_maker_slideshow_maker.adapter.StickerListAdapter
import kotlinx.android.synthetic.main.layout_choose_sticker.view.*

class ChooseStickerLayout : LinearLayout {

    private val mStickerListAdapter = StickerListAdapter {
        callback?.onSelectSticker(BuildConfig.API_URL + it)
    }

    var callback: StickerCallback? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        inflate(context, R.layout.layout_choose_sticker, this)

        stickerListView.adapter = mStickerListAdapter
        stickerListView.layoutManager = GridLayoutManager(context, 5)
        mStickerListAdapter.setItemList(StickerFileAsset.bubbleList())

        initActions()
    }

    private fun initActions() {
        collectionView1.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.bubbleList())
        }

        collectionView2.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.rainbowList())
        }

        collectionView3.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.cartoonList())
        }

        collectionView4.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.childList())
        }

        collectionView5.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.flowerList())
        }

        collectionView6.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.amojiList())
        }

        collectionView7.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.deliciousList())
        }

        collectionView8.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.handList())
        }

        collectionView9.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.popularList())
        }

        collectionView10.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.valentineList())
        }

        collectionView11.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.emojList())
        }

        collectionView12.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.rageList())
        }

        collectionView13.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.christmasList())
        }

        collectionView14.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.unicornList())
        }

        collectionView15.setOnClickListener {
            mStickerListAdapter.setItemList(StickerFileAsset.stickerList())
        }
    }

    interface StickerCallback {
        fun onSelectSticker(stickerPath: String)
    }
}