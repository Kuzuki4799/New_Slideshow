package com.acatapps.videomaker.ui.edit_video

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.GSEffectListAdapter
import com.acatapps.videomaker.adapter.SlideSourceAdapter
import com.acatapps.videomaker.base.BaseSlideShow
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.data.StickerForRenderData
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.ffmpeg.FFmpeg
import com.acatapps.videomaker.ffmpeg.FFmpegCmd
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import com.acatapps.videomaker.ui.pick_media.PickMediaActivity
import com.acatapps.videomaker.ui.process_video.ProcessVideoActivity
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.Utils
import com.acatapps.videomaker.video_player_slide.VideoPlayerSlideGLView
import com.acatapps.videomaker.video_player_slide.VideoPlayerSlideRenderer
import kotlinx.android.synthetic.main.activity_base_tools_edit.*
import kotlinx.android.synthetic.main.activity_base_tools_edit.videoControllerView
import kotlinx.android.synthetic.main.layout_change_effect_tools.view.*

class VideoSlideActivity : BaseSlideShow() , MediaPlayer.OnCompletionListener {

    private val mVideoPathList = ArrayList<String>()
    private var mIsPlaying = true

    private lateinit var mVideoPlayerSlideGLView: VideoPlayerSlideGLView
    private lateinit var mVideoSlideRenderer: VideoPlayerSlideRenderer
    private var mCurrentVideoIndex = 0

    private var mTimelineOffset = 0

    private var totalDuration  = 0
    private var mCurrentTime = 0
    private var mVideoVolume = 1f
    override fun isImageSlideShow(): Boolean = false
    private val mSlideSourceAdapter = SlideSourceAdapter()

    private val mGSEffectListAdapter = GSEffectListAdapter()
    private var mVideoSlideDataList = ArrayList<VideoInSlideData>()

    override fun doInitViews() {
        needShowDialog = true


        setScreenTitle(getString(R.string.edit_video))
        changeThemeTools.visibility = View.GONE
        changeTransitionTools.visibility = View.GONE
        changeDurationTools.visibility = View.GONE
        changeFilterTools.visibility = View.GONE
        changeTrimsTools.visibility = View.GONE

        intent.getStringArrayListExtra("Video picked list")?.let {
            mVideoPathList.apply {
                clear()
                addAll(it)
            }

        }
        for (item in mVideoPathList) {
            totalDuration += (MediaUtils.getVideoDuration(item))
            mVideoSlideDataList.add(VideoInSlideData(item, View.generateViewId(), GSEffectUtils.EffectType.NONE))
        }
        videoControllerView.setMaxDuration(totalDuration)
        mVideoPlayerSlideGLView = VideoPlayerSlideGLView(this, null)
        mVideoSlideRenderer = VideoPlayerSlideRenderer(mVideoPathList[mCurrentVideoIndex], this, mVideoPlayerSlideGLView) {
            mCurrentTime = mTimelineOffset+it

            runOnUiThread {
                videoControllerView.setCurrentDuration(mTimelineOffset+it)
                checkInTime(mTimelineOffset+it)
            }

        }
        mVideoPlayerSlideGLView.performSetRenderer(mVideoSlideRenderer)

        setGLView(mVideoPlayerSlideGLView)
        doPlayVideo()

        toolType = ToolType.EFFECT
        showLayoutChangeEffect()
    }


    override fun doInitActions() {

        setRightButton (R.drawable.ic_save_vector){
            doExportVideo()
        }
        videoControllerView.onChangeListener = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }
        mVideoPlayerSlideGLView.setOnClickListener {
            if(onEditSticker) return@setOnClickListener
            if(mDoExport) {
                mDoExport = false
                mIsPlaying = true
                icPlay.visibility = View.GONE
                doSeekTo(0)
            } else {
                if (mIsPlaying) doPauseVideo()
                else doPlayVideo()
            }

        }

        changeEffectTools.setOnClickListener {
            if (toolType == ToolType.EFFECT) return@setOnClickListener
            toolType = ToolType.EFFECT
            showLayoutChangeEffect()
        }

        mSlideSourceAdapter.onClickItem = {
            var timeMs = 0
            for(item in 0 until it) {
                timeMs += MediaUtils.getVideoDuration(mVideoPathList[item])
            }
            doSeekTo(timeMs)
            mGSEffectListAdapter.selectEffect(mVideoSlideDataList[it].gsEffectType)
        }

        mGSEffectListAdapter.onSelectEffectCallback = {position, gsEffectType ->
            mVideoSlideDataList[mCurrentVideoIndex].gsEffectType = gsEffectType
            var timeMs = 0
            for(item in 0 until mCurrentVideoIndex) {
                timeMs += MediaUtils.getVideoDuration(mVideoPathList[item])
            }
            doSeekTo(timeMs)
        }

    }

    override fun getCurrentVideoTimeMs(): Int = mCurrentTime

    override fun performPlayVideo() {
        doPlayVideo()
    }

    override fun performPauseVideo() {
        doPauseVideo()
    }

    override fun getMaxDuration(): Int=totalDuration

    override fun performSeekTo(timeMs: Int) {
        doSeekTo(timeMs)
    }

    override fun performSeekTo(timeMs: Int, showProgress: Boolean) {
        doSeekTo(timeMs)

    }

    override fun isPlaying(): Boolean = mIsPlaying

    override fun getSourcePathList(): ArrayList<String> = mVideoPathList

    override fun getScreenTitle(): String = getString(R.string.edit_video)

    override fun performExportVideo() {
        doExportVideo()
    }

    override fun performChangeVideoVolume(volume: Float) {
        mVideoVolume = volume
        mVideoSlideRenderer.changeVideoVolume(mVideoVolume)
        Logger.e("change volume = $mVideoVolume")
    }

    private fun doPauseVideo() {
        mIsPlaying = false
        mVideoSlideRenderer.onPause()
        mVideoPlayerSlideGLView.onPause()
        icPlay.visibility = View.VISIBLE
        onPauseVideo()
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mVideoSlideRenderer.onPlayVideo()
        icPlay.visibility = View.GONE
        onPlayVideo()
    }

    override fun onPause() {
        super.onPause()
        doPauseVideo()
        mVideoPlayerSlideGLView.onPause()
    }

    override fun onCompletion(mp: MediaPlayer?) {
     onNextVideo()
    }


    private fun onNextVideo() {
        if(mDestroy) return
        Logger.e("current index = $mCurrentVideoIndex")
        if (mCurrentVideoIndex + 1 >= mVideoPathList.size) {
            mCurrentVideoIndex = 0
            doSeekTo(0)
            updateTimelineOffset()
            mVideoPlayerSlideGLView.changeVideo(mVideoSlideDataList[mCurrentVideoIndex])
        } else {
            mCurrentVideoIndex++
            updateTimelineOffset()
            mVideoPlayerSlideGLView.changeVideo(mVideoSlideDataList[mCurrentVideoIndex])

        }
        updateEffectHighlight()
        mSlideSourceAdapter.changeVideo(mCurrentVideoIndex)

    }
    private fun updateTimelineOffset() {
        mTimelineOffset = 0
        if (mCurrentVideoIndex == 0) mTimelineOffset = 0
        else
            for (index in 0 until mCurrentVideoIndex) {
                mTimelineOffset += (MediaUtils.getVideoDuration(mVideoPathList[index]))
            }
    }
    private fun doSeekTo(timeMilSec: Int) {
        var time = 0
        var targetIndex = 0
        for (item in mVideoPathList) {
            val duration = MediaUtils.getVideoDuration(item)
            if (time + duration > timeMilSec) {
                mCurrentVideoIndex = targetIndex
                mVideoPlayerSlideGLView.seekTo(mVideoSlideDataList[mCurrentVideoIndex], timeMilSec - time, mIsPlaying)
                break
            } else {
                targetIndex++
                time += duration
            }
        }
        updateEffectHighlight()
        mSlideSourceAdapter.changeVideo(mCurrentVideoIndex)
        updateTimelineOffset()
        onSeekTo(timeMilSec)
    }
    private fun updateEffectHighlight(){
        Logger.e("effect in ${mCurrentVideoIndex} = ${mVideoSlideDataList[mCurrentVideoIndex].gsEffectType}")
        mGSEffectListAdapter.selectEffect(mVideoSlideDataList[mCurrentVideoIndex].gsEffectType)
    }
    private fun showLayoutChangeEffect() {
        val view = View.inflate(this, R.layout.layout_change_effect_tools, null)
        showToolsActionLayout(view)

        view.videoInChangeEffect.adapter = mSlideSourceAdapter
        mSlideSourceAdapter.addImagePathList(mVideoPathList)
        view.videoInChangeEffect.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        view.effectListView.adapter = mGSEffectListAdapter
        view.effectListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.icAddPhotoInChangeEffect.setOnClickListener {
            doAddMoreVideo()
        }
    }
    private var addMoreVideoAvailable =true
    private fun doAddMoreVideo() {
        if(addMoreVideoAvailable) {
            addMoreVideoAvailable = false
            val intent = Intent(this, PickMediaActivity::class.java).apply {
                putExtra("action", PickMediaActivity.ADD_MORE_VIDEO)
                putStringArrayListExtra("list-video", mVideoPathList)
            }
            startActivityForResult(intent, PickMediaActivity.ADD_MORE_VIDEO_REQUEST_CODE)
            object :CountDownTimer(1000,1000) {
                override fun onFinish() {
                    addMoreVideoAvailable = true
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()
        }

    }

    private fun doExportVideo() {
        doPauseVideo()
        if(Utils.checkStorageSpace(mVideoPathList)) {

            showExportDialog(true) {quality, ratio ->
                if(quality < 1) {
                    showToast(getString(R.string.please_choose_video_quality))
                } else {
                    setOffAllStickerAndText()
                    mVideoSlideRenderer.onDestroy()
                    mDoExport = true
                    prepareForExport(quality, ratio)
                    dismissExportDialog()
                }
            }

        } else {
            showToast(getString(R.string.free_space_too_low))
        }

    }


    private fun prepareForExport(quality: Int, ratio:Int) {
        showProgressDialog()

        Thread{
            val stickerAddedForRender = ArrayList<StickerForRenderData>()
            val stickerContainerWidth = stickerContainer.width
            val stickerContainerHeight = stickerContainer.height
            for (item in getStickerAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainerWidth,
                    stickerContainerHeight,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.stickerViewId)
                view.draw(Canvas(bitmap))
                val outPath = if(isImageSlideShow()) {
                    FileUtils.saveStickerToTemp(bitmap)
                } else {
                    val outBitmap = getOutSticker(bitmap, ratio)
                    FileUtils.saveStickerToTemp(outBitmap)
                }

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
                view.draw(Canvas(bitmap))

                val outPath = if(isImageSlideShow()) {
                    FileUtils.saveStickerToTemp(bitmap)
                } else {
                    val outBitmap = getOutSticker(bitmap, ratio)
                    FileUtils.saveStickerToTemp(outBitmap)
                }

                stickerAddedForRender.add(
                    StickerForRenderData(
                        outPath,
                        item.startTimeMilSec,
                        item.endTimeMilSec
                    )
                )
            }

            val musicPath = getMusicData()
            val musicVolume = getMusicVolume()
            val finalAudio = FileUtils.getTempMp3OutPutFile()
            if(MediaUtils.getAudioDuration(musicPath) > totalDuration) {
                val cmd = FFmpegCmd.trimAudio(musicPath, 0, totalDuration.toLong(), finalAudio)
                FFmpeg(cmd).runCmd {
                    val intent = Intent(this, ProcessVideoActivity::class.java)
                    Bundle().apply {
                        putSerializable("stickerDataList", stickerAddedForRender)
                        putString("musicPath", finalAudio)
                        putFloat("musicVolume", musicVolume)
                        putFloat("videoVolume", mVideoVolume)
                        putInt("videoQuality", quality)
                        putInt("videoSlideOutRatio", ratio)
                        putSerializable("VideoInSlideData", mVideoSlideDataList)
                        intent.putExtra("bundle", this)
                        intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.renderVideoSlideAction)
                    }

                    runOnUiThread {
                        dismissProgressDialog()
                        startActivity(intent)
                    }
                }
            } else {
                val intent = Intent(this, ProcessVideoActivity::class.java)
                Bundle().apply {
                    putSerializable("stickerDataList", stickerAddedForRender)
                    putString("musicPath", musicPath)
                    putFloat("musicVolume", musicVolume)
                    putInt("videoQuality", quality)
                    putFloat("videoVolume", mVideoVolume)
                    putInt("videoSlideOutRatio", ratio)
                    putSerializable("VideoInSlideData", mVideoSlideDataList)
                    intent.putExtra("bundle", this)
                    intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.renderVideoSlideAction)
                }
                runOnUiThread {
                    dismissProgressDialog()
                    startActivity(intent)
                }
            }

        }.start()
    }

    private fun getOutSticker(bitmap: Bitmap, ratio:Int):Bitmap {
        val stickerContainerWidth = stickerContainer.width
        val stickerContainerHeight = stickerContainer.height
        if(ratio == 2) {
            val outBitmap = Bitmap.createBitmap( stickerContainerWidth*9/16,stickerContainerHeight, Bitmap.Config.ARGB_8888)
            Canvas(outBitmap).apply {
                drawBitmap(bitmap, -stickerContainerWidth*7f/32, 0f,null)
            }
            return outBitmap
        } else if(ratio == 1) {
            val outBitmap = Bitmap.createBitmap( stickerContainerWidth,stickerContainerHeight*9/16, Bitmap.Config.ARGB_8888)
            Canvas(outBitmap).apply {
                drawBitmap(bitmap, 0f,-stickerContainerWidth*7f/32,null)
            }
            return outBitmap
        } else return bitmap

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.e("request code = $requestCode")
        if(requestCode == PickMediaActivity.ADD_MORE_VIDEO_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                data?.let {
                    val pathList = it.getStringArrayListExtra("Video picked list") ?: ArrayList()
                    Logger.e("size = ${pathList.size}")
                    pathList.let {pathList->
                        Logger.e("video add more = ${pathList.size}")
                        doPauseVideo()
                        mVideoPathList.clear()
                        mVideoPathList.addAll(pathList)
                        mSlideSourceAdapter.addImagePathList(pathList)

                        totalDuration = 0
                        val videoEffectHashMap = HashMap<String, GSEffectUtils.EffectType>()
                        for(item in mVideoSlideDataList) {
                            videoEffectHashMap[item.path] = item.gsEffectType
                        }
                        mVideoSlideDataList.clear()
                        for (item in mVideoPathList) {
                            totalDuration += (MediaUtils.getVideoDuration(item))
                            mVideoSlideDataList.add(VideoInSlideData(item, View.generateViewId(), videoEffectHashMap[item] ?: GSEffectUtils.EffectType.NONE))
                        }
                        videoControllerView.setMaxDuration(totalDuration)

                    }
                }
                showProgressDialog()
                object :CountDownTimer(1000, 1000) {
                    override fun onFinish() {
                        doSeekTo(0)
                        runOnUiThread {
                            dismissProgressDialog()
                        }
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()


            }

        }
    }

    override fun isShowAds(): Boolean {
        return true
    }

    private var mDoExport = false
    override fun onResume() {
        super.onResume()
        mVideoPlayerSlideGLView.onResume()

    }

    private var mDestroy = false
    override fun onDestroy() {
        mDestroy = true
        mVideoSlideRenderer.onDestroy()
        super.onDestroy()
        Logger.e("video slide -------->on destroy")
    }
}
