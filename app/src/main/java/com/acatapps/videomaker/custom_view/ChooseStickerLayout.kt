package com.acatapps.videomaker.custom_view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.StickerListAdapter
import com.acatapps.videomaker.utils.DimenUtils
import kotlinx.android.synthetic.main.layout_choose_sticker.view.*
import kotlin.math.roundToInt

class ChooseStickerLayout:LinearLayout {

    private val mStickerListAdapter = StickerListAdapter{
        callback?.onSelectSticker(it)
    }

    private val mBaseStickerPath = "file:///android_asset/"
    private val collection1 = "sticker/collection1"
    private val collection2 = "sticker/collection2"
    private val collection3 = "sticker/collection3"
    private val collection4 = "sticker/collection4"
    private val collection5 = "sticker/collection5"
    private val collection6 = "sticker/collection6"
    private val collection7 = "sticker/collection7"
    private val collection8 = "sticker/collection8"
    private val collection9 = "sticker/collection9"

    var callback:StickerCallback? = null

    constructor(context: Context?) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
       inflate(context, R.layout.layout_choose_sticker, this)

        val col = DimenUtils.screenWidth(context)/(DimenUtils.density(context)*56)
        stickerListView.adapter = mStickerListAdapter
        stickerListView.layoutManager = GridLayoutManager(context, 5)
        mStickerListAdapter.setItemList(getStickerCollection1())

        initActions()
    }

    private fun initActions() {
        collectionView1.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection1())
        }

        collectionView2.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection2())
        }

        collectionView3.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection3())
        }

        collectionView4.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection4())
        }

        collectionView5.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection5())
        }

        collectionView6.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection6())
        }

        collectionView7.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection7())
        }


        collectionView8.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection8())
        }

        collectionView9.setOnClickListener {
            mStickerListAdapter.setItemList(getStickerCollection9())
        }
    }

    private fun getStickerCollection1():ArrayList<String> {

        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_catus_1.toString())
        outList.add(R.drawable.ic_catus_2.toString())
        outList.add(R.drawable.ic_catus_3.toString())
        outList.add(R.drawable.ic_catus_4.toString())
        outList.add(R.drawable.ic_catus_5.toString())
        outList.add(R.drawable.ic_catus_6.toString())
        outList.add(R.drawable.ic_catus_7.toString())
        outList.add(R.drawable.ic_catus_8.toString())
        outList.add(R.drawable.ic_catus_9.toString())

        return outList
    }

    private fun getStickerCollection2():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_cat1.toString())
        outList.add(R.drawable.ic_cat2.toString())
        outList.add(R.drawable.ic_cat3.toString())
        outList.add(R.drawable.ic_cat4.toString())
        outList.add(R.drawable.ic_cat5.toString())
        outList.add(R.drawable.ic_cat6.toString())
        outList.add(R.drawable.ic_cat7.toString())
        outList.add(R.drawable.ic_cat8.toString())
        outList.add(R.drawable.ic_cat9.toString())
        outList.add(R.drawable.ic_cat10.toString())
        outList.add(R.drawable.ic_cat11.toString())
        outList.add(R.drawable.ic_cat12.toString())
        outList.add(R.drawable.ic_cat13.toString())
        outList.add(R.drawable.ic_cat14.toString())
        outList.add(R.drawable.ic_cat15.toString())
        outList.add(R.drawable.ic_cat16.toString())
        outList.add(R.drawable.ic_cat17.toString())
        outList.add(R.drawable.ic_cat18.toString())
        outList.add(R.drawable.ic_cat19.toString())
        outList.add(R.drawable.ic_cat20.toString())
        outList.add(R.drawable.ic_cat21.toString())
        return outList
    }

    private fun getStickerCollection3():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_eyes1.toString())
        outList.add(R.drawable.ic_eyes2.toString())
        outList.add(R.drawable.ic_eyes3.toString())
        outList.add(R.drawable.ic_eyes4.toString())
        outList.add(R.drawable.ic_eyes5.toString())
        outList.add(R.drawable.ic_eyes6.toString())
        outList.add(R.drawable.ic_eyes7.toString())
        outList.add(R.drawable.ic_eyes8.toString())
        outList.add(R.drawable.ic_eyes9.toString())
        outList.add(R.drawable.ic_eyes10.toString())
        outList.add(R.drawable.ic_eyes11.toString())
        outList.add(R.drawable.ic_eyes12.toString())
        outList.add(R.drawable.ic_eyes13.toString())
        outList.add(R.drawable.ic_eyes14.toString())
        outList.add(R.drawable.ic_eyes15.toString())
        outList.add(R.drawable.ic_eyes16.toString())
        outList.add(R.drawable.ic_eyes17.toString())
        outList.add(R.drawable.ic_eyes18.toString())
        outList.add(R.drawable.ic_eyes19.toString())
        outList.add(R.drawable.ic_eyes20.toString())

        return outList
    }

    private fun getStickerCollection4():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_fruit1.toString())
        outList.add(R.drawable.ic_fruit2.toString())
        outList.add(R.drawable.ic_fruit3.toString())
        outList.add(R.drawable.ic_fruit4.toString())
        outList.add(R.drawable.ic_fruit5.toString())
        outList.add(R.drawable.ic_fruit6.toString())
        outList.add(R.drawable.ic_fruit7.toString())
        outList.add(R.drawable.ic_fruit8.toString())
        outList.add(R.drawable.ic_fruit9.toString())

        return outList
    }

    private fun getStickerCollection5():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_common1.toString())
        outList.add(R.drawable.ic_common2.toString())
        outList.add(R.drawable.ic_common3.toString())
        outList.add(R.drawable.ic_common4.toString())
        outList.add(R.drawable.ic_common5.toString())
        outList.add(R.drawable.ic_common6.toString())
        outList.add(R.drawable.ic_common7.toString())
        outList.add(R.drawable.ic_common8.toString())
        outList.add(R.drawable.ic_common9.toString())
        outList.add(R.drawable.ic_common10.toString())
        outList.add(R.drawable.ic_common11.toString())
        outList.add(R.drawable.ic_common12.toString())
        outList.add(R.drawable.ic_common13.toString())
        outList.add(R.drawable.ic_common14.toString())
        outList.add(R.drawable.ic_common15.toString())
        outList.add(R.drawable.ic_common16.toString())
        outList.add(R.drawable.ic_common17.toString())
        outList.add(R.drawable.ic_common18.toString())
        outList.add(R.drawable.ic_common19.toString())
        outList.add(R.drawable.ic_common20.toString())
        outList.add(R.drawable.ic_common21.toString())
        outList.add(R.drawable.ic_common22.toString())
        outList.add(R.drawable.ic_common23.toString())
        outList.add(R.drawable.ic_common24.toString())
        return outList
    }

    private fun getStickerCollection6():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_heart1.toString())
        outList.add(R.drawable.ic_heart2.toString())
        outList.add(R.drawable.ic_heart3.toString())
        outList.add(R.drawable.ic_heart4.toString())
        outList.add(R.drawable.ic_heart5.toString())
        outList.add(R.drawable.ic_heart6.toString())
        outList.add(R.drawable.ic_heart7.toString())
        outList.add(R.drawable.ic_heart8.toString())
        outList.add(R.drawable.ic_heart9.toString())
        outList.add(R.drawable.ic_heart10.toString())
        outList.add(R.drawable.ic_heart11.toString())
        outList.add(R.drawable.ic_heart12.toString())
        outList.add(R.drawable.ic_heart13.toString())
        outList.add(R.drawable.ic_heart14.toString())
        outList.add(R.drawable.ic_heart15.toString())
        outList.add(R.drawable.ic_heart16.toString())
        outList.add(R.drawable.ic_heart17.toString())
        outList.add(R.drawable.ic_heart18.toString())
        outList.add(R.drawable.ic_heart19.toString())
        outList.add(R.drawable.ic_heart20.toString())
        outList.add(R.drawable.ic_heart21.toString())
        outList.add(R.drawable.ic_heart22.toString())
        outList.add(R.drawable.ic_heart23.toString())
        outList.add(R.drawable.ic_heart24.toString())
        outList.add(R.drawable.ic_heart25.toString())

        return outList
    }
    private fun getStickerCollection7():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_ice1.toString())
        outList.add(R.drawable.ic_ice2.toString())
        outList.add(R.drawable.ic_ice3.toString())
        outList.add(R.drawable.ic_ice4.toString())
        outList.add(R.drawable.ic_ice5.toString())
        outList.add(R.drawable.ic_ice6.toString())
        outList.add(R.drawable.ic_ice7.toString())
        outList.add(R.drawable.ic_ice8.toString())
        outList.add(R.drawable.ic_ice9.toString())
        outList.add(R.drawable.ic_ice10.toString())
        outList.add(R.drawable.ic_ice11.toString())
        outList.add(R.drawable.ic_ice12.toString())
        outList.add(R.drawable.ic_ice13.toString())
        outList.add(R.drawable.ic_ice14.toString())
        outList.add(R.drawable.ic_ice15.toString())
        outList.add(R.drawable.ic_ice16.toString())
        outList.add(R.drawable.ic_ice17.toString())
        outList.add(R.drawable.ic_ice18.toString())
        outList.add(R.drawable.ic_ice19.toString())
        outList.add(R.drawable.ic_ice20.toString())
        outList.add(R.drawable.ic_ice21.toString())

        return outList
    }
    private fun getStickerCollection8():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_music1.toString())
        outList.add(R.drawable.ic_music2.toString())
        outList.add(R.drawable.ic_music3.toString())
        outList.add(R.drawable.ic_music4.toString())
        outList.add(R.drawable.ic_music5.toString())
        outList.add(R.drawable.ic_music6.toString())
        outList.add(R.drawable.ic_music7.toString())
        outList.add(R.drawable.ic_music8.toString())
        outList.add(R.drawable.ic_music9.toString())
        outList.add(R.drawable.ic_music10.toString())
        outList.add(R.drawable.ic_music11.toString())
        outList.add(R.drawable.ic_music12.toString())
        outList.add(R.drawable.ic_music13.toString())
        outList.add(R.drawable.ic_music14.toString())
        return outList
    }
    private fun getStickerCollection9():ArrayList<String> {
        val outList = ArrayList<String>()
        outList.add(R.drawable.ic_restaurent1.toString())
        outList.add(R.drawable.ic_restaurent2.toString())
        outList.add(R.drawable.ic_restaurent3.toString())
        outList.add(R.drawable.ic_restaurent4.toString())
        outList.add(R.drawable.ic_restaurent5.toString())
        outList.add(R.drawable.ic_restaurent6.toString())
        outList.add(R.drawable.ic_restaurent7.toString())
        outList.add(R.drawable.ic_restaurent8.toString())
        outList.add(R.drawable.ic_restaurent9.toString())
        outList.add(R.drawable.ic_restaurent10.toString())
        outList.add(R.drawable.ic_restaurent11.toString())
        outList.add(R.drawable.ic_restaurent12.toString())
        outList.add(R.drawable.ic_restaurent13.toString())
        outList.add(R.drawable.ic_restaurent14.toString())
        outList.add(R.drawable.ic_restaurent15.toString())
        outList.add(R.drawable.ic_restaurent16.toString())
        return outList
    }
    private fun getAllFilePathInAsset(folderName:String):ArrayList<String> {
        val pathList = ArrayList<String>()
        val imageNames = context.resources.assets.list(folderName) ?: arrayOf()
        for (image in imageNames) {
            pathList.add("$mBaseStickerPath$folderName/$image")
        }
        return pathList
    }

    interface StickerCallback {
        fun onSelectSticker(stickerPath:String)
    }

}