package com.acatapps.videomaker.ui.join_video

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.acatapps.videomaker.MainActivity
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.VideoInJoinerAdapter
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.models.VideoForJoinDataModel
import com.acatapps.videomaker.ui.process_video.ProcessVideoActivity
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.Utils
import com.acatapps.videomaker.video_player_slide.VideoPlayerSlideRenderer
import kotlinx.android.synthetic.main.activity_join_video.*

class JoinVideoActivity : BaseActivity(), MediaPlayer.OnCompletionListener {
    override fun getContentResId(): Int = R.layout.activity_join_video

    private var mCurrentVideoIndex = 0
    private val mVideoPathList = ArrayList<String>()
    private val mVideoDataList = ArrayList<VideoForJoinDataModel>()
    private lateinit var mVideoSlideRenderer: VideoPlayerSlideRenderer

    private var mTimelineOffset = 0

    private val mVideoInJoinerAdapter = VideoInJoinerAdapter()
    private var mTimer: CountDownTimer? = null

    private var mIsPlaying = true

    companion object {
        fun gotoActivity(activity: Activity, videoPathList: ArrayList<String>) {
            val intent = Intent(activity, JoinVideoActivity::class.java)
            val bundle = Bundle().apply {
                putStringArrayList("videoPathList", videoPathList)
            }
            intent.putExtra("bundle", bundle)
            activity.startActivity(intent)
        }
    }

    override fun isShowAds(): Boolean = true

    override fun initViews() {
        val scale = DimenUtils.videoScaleInTrim()
        bgView.layoutParams.width = (DimenUtils.screenWidth(this)*scale).toInt()
        bgView.layoutParams.height = (DimenUtils.screenWidth(this)*scale).toInt()
        setScreenTitle(getString(R.string.join))
        intent.getBundleExtra("bundle")?.let {
            it.getStringArrayList("videoPathList")?.let { pathList ->
                if (pathList.size > 0) {
                    setupData(pathList)
                    setupListView()
                }
            }
        }
        needShowDialog = true

    }



    override fun initActions() {
        videoControllerView.onChangeListener = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }


        buttonJoinVideo.setClick {
            doPauseVideo()

            if(Utils.checkStorageSpace(mVideoPathList)) {
                mVideoSlideRenderer.onDestroy()
                mDoJoin = true

                val intent = Intent(this@JoinVideoActivity, ProcessVideoActivity::class.java)
                intent.putStringArrayListExtra("joinVideoList", mVideoPathList)
                intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.joinVideoActon)
                startActivity(intent)



            } else {
                showToast(getString(R.string.free_space_too_low))
            }


        }

        videoPlayerView.setOnClickListener {
            if(mDoJoin) {
                onRestartVideo()
            } else {
                if (mIsPlaying) doPauseVideo()
                else doPlayVideo()
            }

        }

        mVideoInJoinerAdapter.itemClick = {
            onSelectItem(it.id)
        }

    }

    private fun doSeekTo(timeMilSec: Int) {
        var time = 0
        var targetIndex = 0
        for (item in mVideoDataList) {
            val duration = MediaUtils.getVideoDuration(item.path)
            if (time + duration > timeMilSec) {
                mCurrentVideoIndex = targetIndex



                mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)
                doPlayVideo()
                break
            } else {
                targetIndex++
                time += duration
            }
        }
        updateTimelineOffset()
    }

    private fun onSelectItem(videoId:Int) {
        var time = 0
        var targetIndex = 0
        icPlay.visibility = View.GONE
        mIsPlaying = true
        for (item in mVideoDataList) {
            if(item.id == videoId) {
                mCurrentVideoIndex = targetIndex
                mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)
            } else {
                targetIndex++
                time += MediaUtils.getVideoDuration(item.path)
            }
        }
        updateTimelineOffset()
    }

    private fun setupData(imageList: ArrayList<String>) {


        showProgressDialog()

        mVideoPathList.clear()
        mVideoPathList.addAll(imageList)

        var totalDuration = 0
        for (item in mVideoPathList) {
            mVideoDataList.add(VideoForJoinDataModel(item))
            totalDuration += (MediaUtils.getVideoDuration(item))
        }

        dismissProgressDialog()
        videoControllerView.setMaxDuration(totalDuration)

    }
    private var mCurrentDuration = 0
    private fun setupListView() {
        videoListView.apply {
            adapter = mVideoInJoinerAdapter
            layoutManager =
                LinearLayoutManager(this@JoinVideoActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        for(item in mVideoDataList) {
            mVideoInJoinerAdapter.addItem(item)
        }
        mVideoInJoinerAdapter.highlightItem(mVideoDataList[0].id)

    }

    override fun onCompletion(mp: MediaPlayer?) {
        onNextVideo()
    }

    private fun onNextVideo() {
        Logger.e("current index = $mCurrentVideoIndex")
        if (mCurrentVideoIndex + 1 >= mVideoPathList.size) {
            mCurrentVideoIndex = 0
            doSeekTo(0)
            updateTimelineOffset()
            mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)
        } else {

            mCurrentVideoIndex++
            updateTimelineOffset()
            mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)

        }

    }

    private fun updateTimelineOffset() {
        mTimelineOffset = 0
        if (mCurrentVideoIndex == 0) mTimelineOffset = 0
        else
            for (index in 0 until mCurrentVideoIndex) {
                mTimelineOffset += (MediaUtils.getVideoDuration(mVideoPathList[index]))
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoSlideRenderer.onDestroy()
        mTimer?.cancel()
    }


    override fun onPause() {
        super.onPause()
        doPauseVideo()
    }



    private var mDoJoin = false
    private fun onRestartVideo() {
        mDoJoin = false
        mIsPlaying = true
        icPlay.visibility = View.GONE
        doSeekTo(mCurrentDuration)

    }

    private fun doPauseVideo() {
        mIsPlaying = false
        mVideoSlideRenderer.onPause()
        mTimer?.cancel()
        icPlay.visibility = View.VISIBLE
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mVideoSlideRenderer.onPlayVideo()
        mTimer?.start()
        icPlay.visibility = View.GONE
    }

}
