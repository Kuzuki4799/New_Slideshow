package com.hope_studio.video_maker_slideshow_maker.ui.slide_show_v2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.adapter.*
import com.hope_studio.video_maker_slideshow_maker.base.BaseSlideShow
import com.hope_studio.video_maker_slideshow_maker.custom_view.EditTextSticker
import com.hope_studio.video_maker_slideshow_maker.custom_view.StickerView
import com.hope_studio.video_maker_slideshow_maker.custom_view.VideoControllerView
import com.hope_studio.video_maker_slideshow_maker.data.StickerForRenderData
import com.hope_studio.video_maker_slideshow_maker.ho_drawer.ImageSlideGLView
import com.hope_studio.video_maker_slideshow_maker.ho_drawer.ImageSlideRenderer
import com.hope_studio.video_maker_slideshow_maker.ho_drawer.drawer.ImageSlideDataContainer
import com.hope_studio.video_maker_slideshow_maker.ho_theme.data.ThemeData
import com.hope_studio.video_maker_slideshow_maker.ho_transition.GSTransitionUtils
import com.hope_studio.video_maker_slideshow_maker.ho_transition.transition.GSTransition
import com.hope_studio.video_maker_slideshow_maker.ui.pick_media.PickMediaActivity
import com.hope_studio.video_maker_slideshow_maker.ui.process_video.ProcessVideoActivity
import com.hope_studio.video_maker_slideshow_maker.utils.*
import kotlinx.android.synthetic.main.activity_base_tools_edit.*
import kotlinx.android.synthetic.main.layout_change_duration_tools.view.*
import kotlinx.android.synthetic.main.layout_change_filter_tools.view.*
import kotlinx.android.synthetic.main.layout_change_transition_tools.view.*
import java.io.File

class ImageSlideShowActivity : BaseSlideShow() {

    private lateinit var mImageGLView: ImageSlideGLView
    private lateinit var mImageSlideRenderer: ImageSlideRenderer

    companion object {
        val imagePickedListKey = "Image picked list"
    }

    @Volatile
    private lateinit var mImageSlideDataContainer: ImageSlideDataContainer

    private val mImageList = ArrayList<String>()
    private var mTimer: CountDownTimer? = null
    private var mCurrentTimeMs = 0
    private var mIsPlaying = false
    private var mShouldReload = false
    private val mSlideSourceAdapter = SlideSourceAdapter()
    private var mThemeData = ThemeData()
    private var mGsTransition = getRandomTransition()

    private fun getRandomTransition(): GSTransition {
        val randomType = GSTransitionUtils.TransitionType.values().random()
        Logger.e("random type = $randomType")
        return GSTransitionUtils.getTransitionByType(randomType)
    }

    private val mGSTransitionListAdapter = GSTransitionListAdapter {
        mGsTransition = it.gsTransition
        performChangeTransition(it.gsTransition)
    }

    private val mImageWithLookupAdapter = ImageWithLookupAdapter {
        doSeekById(it)
    }


    private val mLookupListAdapter = LookupListAdapter {
        mImageWithLookupAdapter.changeLookupOfCurretItem(it)
        reloadInTime(mCurrentTimeMs)
    }

    override fun isImageSlideShow(): Boolean = true

    override fun doInitViews() {
        useDefaultMusic()
        setScreenTitle(getString(R.string.slide_show))
        changeEffectTools.visibility = View.GONE
        changeTrimsTools.visibility = View.GONE
        needShowDialog = true
        val themeFileName = intent.getStringExtra("themeFileName") ?: ""
        if (themeFileName.isNotEmpty()) {
            mThemeData = ThemeData("none")
        }

        mImageGLView = ImageSlideGLView(this, null)
        mImageSlideRenderer = ImageSlideRenderer(mGsTransition)
        mImageGLView.doSetRenderer(mImageSlideRenderer)
        setGLView(mImageGLView)
        showProgressDialog()
        val imageList = intent.getStringArrayListExtra(imagePickedListKey)

        if (imageList == null || imageList.size < 1) {
            finishAfterTransition()
        } else {
            onInitSlide(imageList)
        }

        toolType = ToolType.TRANSITION
        showLayoutChangeTransition()
    }

    private fun onInitSlide(pathList: ArrayList<String>) {
        mImageList.clear()
        mCurrentTimeMs = 0
        mImageList.addAll(pathList)
        mSlideSourceAdapter.addImagePathList(mImageList)
        Thread {
            mImageSlideDataContainer = ImageSlideDataContainer(mImageList)
            runOnUiThread {
                setMaxTime(mImageSlideDataContainer.getMaxDurationMs())
                dismissProgressDialog()
                doPlayVideo()
                playVideo()
                if (mThemeData.themeVideoFilePath != "none")
                    performChangeTheme(mThemeData)
            }
        }.start()
    }

    private var mCurrentFrameId = 0L
    private fun playVideo() {
        mTimer = object : CountDownTimer(4000000, 40) {
            override fun onFinish() {
                start()
            }

            override fun onTick(millisUntilFinished: Long) {
                if (mIsPlaying) {
                    mCurrentTimeMs += 40
                    if (mCurrentTimeMs >= mImageSlideDataContainer.getMaxDurationMs()) {

                        doRepeat()
                    } else {
                        updateTimeline()
                        val frameData = mImageSlideDataContainer.getFrameDataByTime(mCurrentTimeMs)
                        if (frameData.slideId != mCurrentFrameId) {
                            mImageSlideRenderer.resetData()
                            mCurrentFrameId = frameData.slideId
                        }
                        mImageSlideRenderer.changeFrameData(frameData)
                        onStick()

                    }

                }
            }
        }.start()
    }

    private var mCurrentLookupType = LookupUtils.LookupType.NONE
    private fun onStick() {
        val position =
            mCurrentTimeMs / (mImageSlideDataContainer.transitionTimeMs + mImageSlideDataContainer.delayTimeMs)
        mSlideSourceAdapter.changeHighlightItem(position)
        mCurrentLookupType = mImageWithLookupAdapter.changeHighlightItem(position)
        mLookupListAdapter.highlightItem(mCurrentLookupType)

    }

    override fun doInitActions() {


        setRightButton(R.drawable.ic_save) {
            doExportVideo()
        }

        videoControllerView.onChangeListener = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }

        mImageGLView.setOnClickListener {
            if (onEditSticker) return@setOnClickListener
            if (mShouldReload) {
                mCurrentTimeMs = 0
                mShouldReload = false
            }
            if (mIsPlaying) {
                doPauseVideo()
            } else {
                doPlayVideo()
            }
        }

        changeTransitionTools.setOnClickListener {
            if (toolType == ToolType.TRANSITION || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.TRANSITION
            showLayoutChangeTransition()
            handlerSetMenu(Menu.TRANSFER)
        }

        changeDurationTools.setOnClickListener {
            if (toolType == ToolType.DURATION || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.DURATION
            showLayoutChangeDuration()
            handlerSetMenu(Menu.TIME)
        }

        changeFilterTools.setOnClickListener {
            if (toolType == ToolType.FILTER || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.FILTER
            showLayoutChangeFilter()
            handlerSetMenu(Menu.FILTER)
        }

        mSlideSourceAdapter.onClickItem = {
            doSeekTo(it * (mImageSlideDataContainer.delayTimeMs + mImageSlideDataContainer.transitionTimeMs))
        }
    }

    enum class Menu {
        TRANSFER, TIME, MUSIC, STICKER, TEXT, FILTER
    }

    private fun handlerSetMenu(currentMenu: Menu) {
        when (currentMenu) {
            Menu.TRANSFER -> {
                changeTransitionTools.setColorFilter(Color.BLACK)
                changeDurationTools.setColorFilter(Color.GRAY)
                changeMusicTools.setColorFilter(Color.GRAY)
                changeStickerTools.setColorFilter(Color.GRAY)
                changeTextTools.setColorFilter(Color.GRAY)
                changeFilterTools.setColorFilter(Color.GRAY)

                txtTransfer.setTextColor(Color.BLACK)
                txtTime.setTextColor(Color.GRAY)
                txtMusic.setTextColor(Color.GRAY)
                txtSticker.setTextColor(Color.GRAY)
                txtText.setTextColor(Color.GRAY)
                txtFilter.setTextColor(Color.GRAY)
            }

            Menu.TIME -> {
                changeTransitionTools.setColorFilter(Color.GRAY)
                changeDurationTools.setColorFilter(Color.BLACK)
                changeMusicTools.setColorFilter(Color.GRAY)
                changeStickerTools.setColorFilter(Color.GRAY)
                changeTextTools.setColorFilter(Color.GRAY)
                changeFilterTools.setColorFilter(Color.GRAY)

                txtTransfer.setTextColor(Color.GRAY)
                txtTime.setTextColor(Color.BLACK)
                txtMusic.setTextColor(Color.GRAY)
                txtSticker.setTextColor(Color.GRAY)
                txtText.setTextColor(Color.GRAY)
                txtFilter.setTextColor(Color.GRAY)

            }

            Menu.MUSIC -> {

                changeTransitionTools.setColorFilter(Color.GRAY)
                changeDurationTools.setColorFilter(Color.GRAY)
                changeMusicTools.setColorFilter(Color.BLACK)
                changeStickerTools.setColorFilter(Color.GRAY)
                changeTextTools.setColorFilter(Color.GRAY)
                changeFilterTools.setColorFilter(Color.GRAY)

                txtTransfer.setTextColor(Color.GRAY)
                txtTime.setTextColor(Color.GRAY)
                txtMusic.setTextColor(Color.BLACK)
                txtSticker.setTextColor(Color.GRAY)
                txtText.setTextColor(Color.GRAY)
                txtFilter.setTextColor(Color.GRAY)
            }

            Menu.STICKER -> {

                changeTransitionTools.setColorFilter(Color.GRAY)
                changeDurationTools.setColorFilter(Color.GRAY)
                changeMusicTools.setColorFilter(Color.GRAY)
                changeStickerTools.setColorFilter(Color.BLACK)
                changeTextTools.setColorFilter(Color.GRAY)
                changeFilterTools.setColorFilter(Color.GRAY)

                txtTransfer.setTextColor(Color.GRAY)
                txtTime.setTextColor(Color.GRAY)
                txtMusic.setTextColor(Color.GRAY)
                txtSticker.setTextColor(Color.BLACK)
                txtText.setTextColor(Color.GRAY)
                txtFilter.setTextColor(Color.GRAY)
            }

            Menu.TEXT -> {

                changeTransitionTools.setColorFilter(Color.GRAY)
                changeDurationTools.setColorFilter(Color.GRAY)
                changeMusicTools.setColorFilter(Color.GRAY)
                changeStickerTools.setColorFilter(Color.GRAY)
                changeTextTools.setColorFilter(Color.BLACK)
                changeFilterTools.setColorFilter(Color.GRAY)

                txtTransfer.setTextColor(Color.GRAY)
                txtTime.setTextColor(Color.GRAY)
                txtMusic.setTextColor(Color.GRAY)
                txtSticker.setTextColor(Color.GRAY)
                txtText.setTextColor(Color.BLACK)
                txtFilter.setTextColor(Color.GRAY)
            }

            Menu.FILTER -> {

                changeTransitionTools.setColorFilter(Color.GRAY)
                changeDurationTools.setColorFilter(Color.GRAY)
                changeMusicTools.setColorFilter(Color.GRAY)
                changeStickerTools.setColorFilter(Color.GRAY)
                changeTextTools.setColorFilter(Color.GRAY)
                changeFilterTools.setColorFilter(Color.BLACK)

                txtTransfer.setTextColor(Color.GRAY)
                txtTime.setTextColor(Color.GRAY)
                txtMusic.setTextColor(Color.GRAY)
                txtSticker.setTextColor(Color.GRAY)
                txtText.setTextColor(Color.GRAY)
                txtFilter.setTextColor(Color.BLACK)
            }
        }
    }

    override fun getCurrentVideoTimeMs(): Int = mCurrentTimeMs
    override fun performPlayVideo() {
        doPlayVideo()
    }

    override fun performPauseVideo() {
        doPauseVideo()
    }

    override fun getMaxDuration(): Int = mImageSlideDataContainer.getMaxDurationMs()

    override fun performSeekTo(timeMs: Int, showProgress: Boolean) {
        Logger.e("timeMs = $timeMs")
        if (timeMs >= mImageSlideDataContainer.getMaxDurationMs()) {
            doRepeat()
        } else {
            doSeekTo(timeMs)
        }

    }

    override fun performSeekTo(timeMs: Int) {
        if (timeMs >= mImageSlideDataContainer.getMaxDurationMs()) {
            doRepeat()
            Logger.e("performSeekTo -> doRepeat()")
            return
        }
        Logger.e("performSeekTo -> doSeekTo(timeMs)")
        doSeekTo(timeMs)
    }

    override fun isPlaying(): Boolean = mIsPlaying
    override fun getSourcePathList(): ArrayList<String> = mImageList
    override fun getScreenTitle(): String = getString(R.string.slide_show)

    override fun performExportVideo() {
        doExportVideo()
    }

    override fun performChangeVideoVolume(volume: Float) {
    }

    private fun showLayoutChangeTransition() {
        val view = View.inflate(this, R.layout.layout_change_transition_tools, null)
        showToolsActionLayout(view)

        view.imageOfSlideShowListViewInChangeTransition.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.imageOfSlideShowListViewInChangeTransition.adapter = mSlideSourceAdapter
        view.gsTransitionListView.adapter = mGSTransitionListAdapter
        view.gsTransitionListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mGSTransitionListAdapter.highlightItem(mGsTransition)

        view.icAddPhotoInChangeTransition.setOnClickListener {
            doAddMoreImage()
        }
    }

    private var addMoreAvailable = true
    private fun doAddMoreImage() {
        if (addMoreAvailable) {
            addMoreAvailable = false
            val intent = Intent(this, PickMediaActivity::class.java).apply {
                putExtra("action", PickMediaActivity.ADD_MORE_PHOTO)
                putStringArrayListExtra("list-photo", mImageList)
            }
            openNewActivityForResult(
                intent, PickMediaActivity.ADD_MORE_PHOTO_REQUEST_CODE,
                isShowAds = true,
                isFinish = false
            )
            object : CountDownTimer(1000, 1000) {
                override fun onFinish() {
                    addMoreAvailable = true
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()
        }

    }

    private fun showLayoutChangeDuration() {
        val view = View.inflate(this, R.layout.layout_change_duration_tools, null)
        showToolsActionLayout(view)
        val totalTimeMs =
            (mImageSlideDataContainer.getCurrentDelayTimeMs() + mImageSlideDataContainer.transitionTimeMs)
        view.changeDurationSeekBar.setCurrentDuration(totalTimeMs / 1000)
        view.totalDurationLabel.text =
            Utils.convertSecToTimeString(mImageSlideDataContainer.getMaxDurationMs() / 1000)

        view.changeDurationSeekBar.setDurationChangeListener({
            doPauseVideo()
            doChangeDelayTime(it)
            mShouldReload = true
            videoControllerView.setCurrentDuration(0)
            view.totalDurationLabel.text =
                Utils.convertSecToTimeString(mImageSlideDataContainer.getMaxDurationMs() / 1000)
            videoControllerView.setMaxDuration(mImageSlideDataContainer.getMaxDurationMs())
        }, {
            doRepeat()


        })
    }

    private fun showLayoutChangeFilter() {
        doPauseVideo()
        val view = View.inflate(this, R.layout.layout_change_filter_tools, null)
        showToolsActionLayout(view)

        view.lookupListView.adapter = mLookupListAdapter
        view.lookupListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        view.imageListView.adapter = mImageWithLookupAdapter.apply {
            setItemList(mImageSlideDataContainer.getSlideList())
        }
        view.numberImageLabel.text =
            "${mImageWithLookupAdapter.itemCount} ${getString(R.string.photos)}"
        view.imageListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        onStick()
    }

    private fun doChangeDelayTime(time: Int) {
        mImageSlideDataContainer.delayTimeMs =
            time * 1000 - mImageSlideDataContainer.transitionTimeMs
    }

    private fun performChangeTheme(themeData: ThemeData) {
        doPauseVideo()
        mImageGLView.changeTheme(themeData)
        doRepeat()
        object : CountDownTimer(100, 100) {
            override fun onFinish() {
                doPlayVideo()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun performChangeTransition(gsTransition: GSTransition) {
        mImageGLView.changeTransition(gsTransition)
    }

    private fun doPauseVideo() {
        if (mIsPlaying == false) return
        mIsPlaying = false
        mImageSlideRenderer.onPause()
        onPauseVideo()
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mImageSlideRenderer.onPlay()
        onPlayVideo()
    }

    private fun doSeekTo(timeMs: Int, showProgress: Boolean = true) {
        val autoPlay = mIsPlaying
        doPauseVideo()

        mImageSlideRenderer.setUpdateTexture(true)
        mCurrentTimeMs = timeMs
        mImageSlideRenderer.seekTheme(mCurrentTimeMs)
        if (showProgress)
            showProgressDialog()
        Thread {
            val frameData = mImageSlideDataContainer.seekTo(timeMs)
            mCurrentFrameId = 1L
            mImageSlideRenderer.resetData()
            runOnUiThread {
                dismissProgressDialog()

                mImageSlideRenderer.changeFrameData(frameData)

                onSeekTo(timeMs)
                if (autoPlay) doPlayVideo()
                else doPauseVideo()


            }
        }.start()

    }

    private fun reloadInTime(timeMs: Int) {
        val autoPlay = mIsPlaying
        doPauseVideo()
        Thread {
            val frameData = mImageSlideDataContainer.seekTo(timeMs, true)
            mCurrentTimeMs = timeMs
            runOnUiThread {
                mImageSlideRenderer.changeFrameData(frameData)
                doSeekTo(timeMs, false)
                if (autoPlay) doPlayVideo()
                else doPauseVideo()
            }
        }.start()
    }

    private fun doSeekById(id: Long) {
        doPauseVideo()
        val timeMs = mImageSlideDataContainer.getStartTimeById(id)
        doSeekTo(timeMs)
        onStick()
    }

    private fun doRepeat() {
        doPauseVideo()
        mImageSlideDataContainer.onRepeat()
        mImageSlideRenderer.resetData()
        doSeekTo(0)
        mCurrentTimeMs = 0
        Logger.e("doRepeat")
        onRepeat()
    }

    override fun onPause() {
        super.onPause()
        mImageGLView.onPause()
        doPauseVideo()
    }

    override fun onResume() {
        super.onResume()
        mImageGLView.onResume()

        Thread {
            mImageList.forEach {
                if (!File(it).exists()) {
                    runOnUiThread {
                        finishAds()
                    }
                }
            }
        }.start()


    }

    override fun onDestroy() {
        super.onDestroy()
        mImageSlideRenderer.onDestroy()
    }

    private fun doExportVideo() {
        doPauseVideo()

        showExportDialog() { quality, ratio ->
            if (quality < 1) {
                showToast(getString(R.string.please_choose_video_quality))
            } else {
                dismissExportDialog()
                prepareForExport(quality)

            }
        }
    }

    private fun prepareForExport(quality: Int) {
        showProgressDialog()
        Thread {
            val stickerAddedForRender = ArrayList<StickerForRenderData>()
            for (item in getStickerAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.stickerViewId)


                if (view is StickerView) view.getOutBitmap(Canvas(bitmap))


                val outPath = FileUtils.saveStickerToTemp(bitmap)
                stickerAddedForRender.add(
                    StickerForRenderData(
                        outPath,
                        item.startTimeMilSec,
                        item.endTimeMilSec
                    )
                )
            }

            for (item in getTextAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.viewId)

                if (view is EditTextSticker) view.getOutBitmap(Canvas(bitmap))
                val outPath = FileUtils.saveStickerToTemp(bitmap)
                stickerAddedForRender.add(
                    StickerForRenderData(
                        outPath,
                        item.startTimeMilSec,
                        item.endTimeMilSec
                    )
                )
            }

            val imageSlideDataList = mImageSlideDataContainer.getSlideList()
            val delayTime = mImageSlideDataContainer.delayTimeMs
            val musicPath = getMusicData()
            val musicVolume = getMusicVolume()
            val themeData = mThemeData

            val intent = Intent(this, ProcessVideoActivity::class.java)
            Bundle().apply {
                putSerializable("stickerDataList", stickerAddedForRender)
                putSerializable("imageDataList", imageSlideDataList)
                putInt("delayTime", delayTime)
                putString("musicPath", musicPath)
                putFloat("musicVolume", musicVolume)
                putSerializable("themeData", themeData)
                putInt("videoQuality", quality)
                putSerializable("gsTransition", mGsTransition)
                intent.putExtra("bundle", this)
                intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.renderSlideAction)
            }
            runOnUiThread {
                dismissProgressDialog()
                openNewActivity(intent, true, false)
            }
        }.start()
    }

    override fun isShowAds(): Boolean {
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.e("request code = $requestCode")
        if (requestCode == PickMediaActivity.ADD_MORE_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val pathList = it.getStringArrayListExtra("Image picked list")

                    pathList?.let { paths ->
                        Logger.e("size result= ${paths.size}")
                        showProgressDialog()
                        Thread {
                            mImageSlideDataContainer.setNewImageList(paths)
                            runOnUiThread {
                                mImageSlideRenderer.setUpdateTexture(true)
                                setMaxTime(mImageSlideDataContainer.getMaxDurationMs())
                                doRepeat()
                                // doPlayVideo()
                                mSlideSourceAdapter.addImagePathList(paths)
                                dismissProgressDialog()
                            }

                        }.start()


                    }
                }
            }
        }
    }


}
