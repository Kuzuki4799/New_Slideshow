package com.acatapps.videomaker.ui.slide_show_v2

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.*
import com.acatapps.videomaker.base.BaseSlideShow
import com.acatapps.videomaker.custom_view.EditTextSticker
import com.acatapps.videomaker.custom_view.StickerView
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.data.StickerForRenderData
import com.acatapps.videomaker.data.ThemeLinkData
import com.acatapps.videomaker.image_slide_show.ImageSlideGLView
import com.acatapps.videomaker.image_slide_show.ImageSlideRenderer
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideDataContainer
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.GSTransitionUtils
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import com.acatapps.videomaker.ui.pick_media.PickMediaActivity
import com.acatapps.videomaker.ui.process_video.ProcessVideoActivity
import com.acatapps.videomaker.utils.*
import kotlinx.android.synthetic.main.activity_base_layout.*
import kotlinx.android.synthetic.main.activity_base_tools_edit.*
import kotlinx.android.synthetic.main.layout_change_duration_tools.view.*
import kotlinx.android.synthetic.main.layout_change_filter_tools.view.*
import kotlinx.android.synthetic.main.layout_change_theme_tools.view.*
import kotlinx.android.synthetic.main.layout_change_transition_tools.view.*
import java.io.File

class ImageSlideShowActivity : BaseSlideShow() {

    private lateinit var mImageGLView:ImageSlideGLView
    private lateinit var mImageSlideRenderer: ImageSlideRenderer

    companion object{
        val imagePickedListKey = "Image picked list"
    }

    @Volatile
    private lateinit var mImageSlideDataContainer: ImageSlideDataContainer

    private val mImageList = ArrayList<String>()
    private var mTimer:CountDownTimer? = null
    private var mCurrentTimeMs = 0
    private var mIsPlaying = false
    private var mShouldReload = false
    private val mSlideSourceAdapter = SlideSourceAdapter()
    private var mThemeData = ThemeData()
    private var mGsTransition = getRandomTransition()

    private fun getRandomTransition():GSTransition {
        val randomType = GSTransitionUtils.TransitionType.values().random()
        Logger.e("random type = $randomType")
        return GSTransitionUtils.getTransitionByType(randomType)
    }



    private val mNewThemeListAdapter = ThemeInHomeAdapter()

    private val mGSTransitionListAdapter = GSTransitionListAdapter {
        mGsTransition = it.gsTransition
        performChangeTransition(it.gsTransition)
    }

    private val mImageWithLookupAdapter = ImageWithLookupAdapter {
        doSeekById(it)
    }


    private val mLookupListAdapter = LookupListAdapter{
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
        if(themeFileName.isNotEmpty()) {
            mThemeData = ThemeData(FileUtils.themeFolderPath+"/${themeFileName}.mp4", ThemeData.ThemType.NOT_REPEAT, themeFileName)
        }

        mImageGLView = ImageSlideGLView(this, null)
        mImageSlideRenderer = ImageSlideRenderer(mGsTransition)
        mImageGLView.doSetRenderer(mImageSlideRenderer)
        setGLView(mImageGLView)
        showProgressDialog()
        val imageList = intent.getStringArrayListExtra(imagePickedListKey)



        if(imageList == null || imageList.size < 1) {
            finishAfterTransition()
        } else {
            onInitSlide(imageList)
        }

        toolType = ToolType.THEME
        showLayoutChangeTheme()

    }

    private fun onInitSlide(pathList:ArrayList<String>) {
        mImageList.clear()
        mCurrentTimeMs = 0
        mImageList.addAll(pathList)
        mSlideSourceAdapter.addImagePathList(mImageList)
        Thread{
            mImageSlideDataContainer = ImageSlideDataContainer(mImageList)
            runOnUiThread {
                setMaxTime(mImageSlideDataContainer.getMaxDurationMs())
                dismissProgressDialog()
                doPlayVideo()
                playVideo()
                if(mThemeData.themeVideoFilePath != "none")
                performChangeTheme(mThemeData)
            }
        }.start()
    }

    private var mCurrentFrameId = 0L
    private fun playVideo() {
        mTimer = object :CountDownTimer(4000000, 40) {
            override fun onFinish() {
                start()
            }

            override fun onTick(millisUntilFinished: Long) {
                if(mIsPlaying) {
                    mCurrentTimeMs += 40
                    if(mCurrentTimeMs >= mImageSlideDataContainer.getMaxDurationMs()) {

                        doRepeat()
                    } else {
                        updateTimeline()
                        val frameData = mImageSlideDataContainer.getFrameDataByTime(mCurrentTimeMs)
                        if(frameData.slideId != mCurrentFrameId) {
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
        val position = mCurrentTimeMs/(mImageSlideDataContainer.transitionTimeMs+mImageSlideDataContainer.delayTimeMs)
        mSlideSourceAdapter.changeHighlightItem(position)
        mCurrentLookupType = mImageWithLookupAdapter.changeHighlightItem(position)
        mLookupListAdapter.highlightItem(mCurrentLookupType)

    }

    override fun doInitActions() {



        setRightButton(R.drawable.ic_save_vector) {
            doExportVideo()
        }

        videoControllerView.onChangeListener = object :VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }

        mImageGLView.setOnClickListener {
            if(onEditSticker) return@setOnClickListener
            if(mShouldReload) {
                mCurrentTimeMs = 0
                mShouldReload = false
            }
            if(mIsPlaying) {
                doPauseVideo()
            } else {
                doPlayVideo()
            }
        }

        changeThemeTools.setOnClickListener {
            if (toolType == ToolType.THEME || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.THEME
            showLayoutChangeTheme()
        }

        changeTransitionTools.setOnClickListener {
            if (toolType == ToolType.TRANSITION || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.TRANSITION
            showLayoutChangeTransition()
        }

        changeDurationTools.setOnClickListener {
            if (toolType == ToolType.DURATION || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.DURATION
            showLayoutChangeDuration()
        }

        changeFilterTools.setOnClickListener {
            if (toolType == ToolType.FILTER || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.FILTER
            showLayoutChangeFilter()
        }

        mSlideSourceAdapter.onClickItem = {
            doSeekTo(it*(mImageSlideDataContainer.delayTimeMs+mImageSlideDataContainer.transitionTimeMs))
        }
    }

    override fun getCurrentVideoTimeMs(): Int = mCurrentTimeMs
    override fun performPlayVideo() {
        doPlayVideo()
    }

    override fun performPauseVideo() {
        doPauseVideo()
    }

    override fun getMaxDuration(): Int =mImageSlideDataContainer.getMaxDurationMs()

    override fun performSeekTo(timeMs: Int, showProgress: Boolean) {
        Logger.e("timeMs = $timeMs")
        if(timeMs >= mImageSlideDataContainer.getMaxDurationMs()) {
            doRepeat()
        } else {
            doSeekTo(timeMs)
        }

    }

    override fun performSeekTo(timeMs: Int) {
        if(timeMs >= mImageSlideDataContainer.getMaxDurationMs()) {
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

    private fun showLayoutChangeTheme() {
        val view = View.inflate(this, R.layout.layout_change_theme_tools, null)
        showToolsActionLayout(view)

        view.imageOfSlideShowListViewInChangeTheme.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.imageOfSlideShowListViewInChangeTheme.adapter = mSlideSourceAdapter

        view.themeListView.adapter = mNewThemeListAdapter
        view.themeListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mNewThemeListAdapter.itemList.clear()
        mNewThemeListAdapter.notifyDataSetChanged()
        mNewThemeListAdapter.addItem(ThemeLinkData("none", "None", "None"))
        ThemeLinkUtils.linkThemeList.forEach {
            mNewThemeListAdapter.addItem(it)
        }
        mNewThemeListAdapter.rewardIsLoaded = true
        mNewThemeListAdapter.notifyDataSetChanged()
        mNewThemeListAdapter.onItemClick = { linkData ->
            doPauseVideo()
            val themFilePath = FileUtils.themeFolderPath+"/${linkData.fileName}.mp4"

            if(linkData.link == "none") {
                val themeData = ThemeData()
                mThemeData = themeData
                performChangeTheme(themeData)

            } else {
                if(File(themFilePath).exists()) {
                    val themeData = ThemeData(FileUtils.themeFolderPath+"/${linkData.fileName}.mp4", ThemeData.ThemType.NOT_REPEAT, linkData.fileName)
                    mThemeData = themeData
                    performChangeTheme(themeData)
                } else {
                    if(checkSettingAutoUpdateTime() == false) {
                        showToast(getString(R.string.please_set_auto_update_time))
                    } else {
                        if(Utils.isInternetAvailable()) {
                            showDownloadThemeDialog(linkData, {
                                val themeData = ThemeData(FileUtils.themeFolderPath+"/${linkData.fileName}.mp4", ThemeData.ThemType.NOT_REPEAT, linkData.fileName)
                                mThemeData = themeData
                                performChangeTheme(themeData)
                                runOnUiThread {
                                    mNewThemeListAdapter.notifyDataSetChanged()
                                }

                            }, {
                                runOnUiThread {
                                    mNewThemeListAdapter.notifyDataSetChanged()
                                }
                            })
                        } else {
                            showToast(getString(R.string.can_t_connect_to_internet))
                        }
                    }



                }
            }

        }

        view.icAddPhotoInChangeTheme.setOnClickListener {
           doAddMoreImage()
        }

    }


    private fun showLayoutChangeTransition() {
        val view = View.inflate(this, R.layout.layout_change_transition_tools, null)
        showToolsActionLayout(view)

        view.imageOfSlideShowListViewInChangeTransition.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.imageOfSlideShowListViewInChangeTransition.adapter = mSlideSourceAdapter
        view.gsTransitionListView.adapter = mGSTransitionListAdapter
        view.gsTransitionListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mGSTransitionListAdapter.highlightItem(mGsTransition)

        view.icAddPhotoInChangeTransition.setOnClickListener {
           doAddMoreImage()
        }
    }

    private var addMoreAvailable = true
    private fun doAddMoreImage() {
        if(addMoreAvailable) {
            addMoreAvailable = false
            val intent = Intent(this, PickMediaActivity::class.java).apply {
                putExtra("action", PickMediaActivity.ADD_MORE_PHOTO)
                putStringArrayListExtra("list-photo", mImageList)
            }
            startActivityForResult(intent, PickMediaActivity.ADD_MORE_PHOTO_REQUEST_CODE)
            object :CountDownTimer(1000,1000) {
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
        val totalTimeMs = (mImageSlideDataContainer.getCurrentDelayTimeMs()+mImageSlideDataContainer.transitionTimeMs)
        view.changeDurationSeekBar.setCurrentDuration(totalTimeMs/1000)
        view.totalDurationLabel.text = Utils.convertSecToTimeString(mImageSlideDataContainer.getMaxDurationMs() / 1000)

        view.changeDurationSeekBar.setDurationChangeListener({
            doPauseVideo()
            doChangeDelayTime(it)
            mShouldReload = true
            videoControllerView.setCurrentDuration(0)
            view.totalDurationLabel.text = Utils.convertSecToTimeString(mImageSlideDataContainer.getMaxDurationMs()/ 1000)
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
        view.lookupListView.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        view.imageListView.adapter = mImageWithLookupAdapter.apply {
            setItemList(mImageSlideDataContainer.getSlideList())
        }
        view.numberImageLabel.text = "${mImageWithLookupAdapter.itemCount} ${getString(R.string.photos)}"
        view.imageListView.layoutManager =  LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        onStick()
    }

    private fun doChangeDelayTime(time:Int) {
        mImageSlideDataContainer.delayTimeMs = time*1000-mImageSlideDataContainer.transitionTimeMs
    }

    private fun performChangeTheme(themeData: ThemeData) {

        doPauseVideo()
        mNewThemeListAdapter.changeCurrentThemeName(themeData.themeName)
        mImageGLView.changeTheme(themeData)
        doRepeat()
        object :CountDownTimer(100,100) {
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
        if(mIsPlaying == false) return
        mIsPlaying = false
        mImageSlideRenderer.onPause()
        onPauseVideo()
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mImageSlideRenderer.onPlay()
        onPlayVideo()
    }

    private fun doSeekTo(timeMs:Int, showProgress: Boolean=true) {
        val autoPlay = mIsPlaying
        doPauseVideo()

        mImageSlideRenderer.setUpdateTexture(true)
        mCurrentTimeMs = timeMs
        mImageSlideRenderer.seekTheme(mCurrentTimeMs)
        if(showProgress)
        showProgressDialog()
        Thread{
            val frameData = mImageSlideDataContainer.seekTo(timeMs)
            mCurrentFrameId = 1L
            mImageSlideRenderer.resetData()
            runOnUiThread {
                dismissProgressDialog()

                mImageSlideRenderer.changeFrameData(frameData)

                onSeekTo(timeMs)
                if(autoPlay) doPlayVideo()
                else doPauseVideo()


            }
        }.start()

    }

    private fun reloadInTime(timeMs:Int) {
        val autoPlay = mIsPlaying
        doPauseVideo()
        Thread{
            val frameData = mImageSlideDataContainer.seekTo(timeMs,true)
            mCurrentTimeMs = timeMs
            runOnUiThread {
                mImageSlideRenderer.changeFrameData(frameData)
                doSeekTo(timeMs, false)
                if(autoPlay) doPlayVideo()
                else doPauseVideo()
            }
        }.start()
    }

    private fun doSeekById(id:Long) {
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

        Thread{
            mImageList.forEach {
                if(!File(it).exists()) {
                    runOnUiThread {
                        finish()
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

        showExportDialog() {quality, ratio ->
            if(quality < 1) {
                showToast(getString(R.string.please_choose_video_quality))
            } else {
                dismissExportDialog()
                prepareForExport(quality)

            }
        }
    }

    private fun prepareForExport(quality:Int) {
        showProgressDialog()
        Thread{
            val stickerAddedForRender = ArrayList<StickerForRenderData>()
            for (item in getStickerAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.stickerViewId)


                if(view is StickerView) view.getOutBitmap(Canvas(bitmap))


                val outPath = FileUtils.saveStickerToTemp(bitmap)
                stickerAddedForRender.add(
                    StickerForRenderData(
                        outPath,
                        item.startTimeMilSec,
                        item.endTimeMilSec
                    )
                )
            }

            for(item in getTextAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.viewId)

                if(view is EditTextSticker) view.getOutBitmap(Canvas(bitmap))
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
                startActivity(intent)
            }
        }.start()
    }

    override fun isShowAds(): Boolean {
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.e("request code = $requestCode")
        if(requestCode == PickMediaActivity.ADD_MORE_PHOTO_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                data?.let {
                    val pathList = it.getStringArrayListExtra("Image picked list")

                    pathList?.let {paths ->
                        Logger.e("size result= ${paths.size}")
                        showProgressDialog()
                        Thread{
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
