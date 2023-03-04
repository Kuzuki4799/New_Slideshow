package com.acatapps.videomaker.ui.join_video

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.acatapps.videomaker.MainActivity
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.VideoInJoinerAdapter
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.models.VideoForJoinDataModel
import com.acatapps.videomaker.ui.process_video.ProcessVideoActivity
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.Utils
import com.acatapps.videomaker.video_player_slide.VideoPlayerSlideRenderer
import kotlinx.android.synthetic.main.activity_join_video.*
import java.io.File

class JoinVideoActivity2 : BaseActivity() {
    override fun getContentResId(): Int = R.layout.activity_join_video

    private var mCurrentVideoIndex = 0

    private val mVideoPathList = ArrayList<String>()
    private val mVideoDataList = ArrayList<VideoForJoinDataModel>()

    private var mTimelineOffset = 0

    private val mVideoInJoinerAdapter = VideoInJoinerAdapter()
    private var mTimer: CountDownTimer? = null

    private var mIsPlaying = true

    companion object {
        fun gotoActivity(activity: Activity, videoPathList: ArrayList<String>) {
            val intent = Intent(activity, JoinVideoActivity2::class.java)
            val bundle = Bundle().apply {
                putStringArrayList("videoPathList", videoPathList)
            }
            intent.putExtra("bundle", bundle)
            activity.startActivity(intent)
        }
    }

    override fun isShowAds(): Boolean = true

    override fun initViews() {

        setScreenTitle(getString(R.string.join))
        videoPlayerView.useController = false
        intent.getBundleExtra("bundle")?.let {
            it.getStringArrayList("videoPathList")?.let { pathList ->
                if (pathList.size > 0) {
                    setupData(pathList)

                }
            }
        }
        needShowDialog = true
        VideoMakerApplication.instance.releaseRewardAd()
    }


    private var mPlayer:ExoPlayer?=null

    private fun changeVideo(path:String) {
        mVideoInJoinerAdapter.highlightItem(mVideoDataList[mCurrentVideoIndex].id)
        if(mPlayer == null) {

            mPlayer = ExoPlayerFactory.newSimpleInstance(this)
            videoPlayerView.player = mPlayer
            mPlayer?.playWhenReady = true
            mPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            mPlayer?.addListener(object : Player.EventListener{


                override fun onSeekProcessed() {

                }




                override fun onLoadingChanged(isLoading: Boolean) {

                }

                override fun onPositionDiscontinuity(reason: Int) {

                }

                override fun onRepeatModeChanged(repeatMode: Int) {

                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

                }



                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    if(playbackState == Player.STATE_ENDED) {

                        Logger.e("on end video ----> Player.STATE_ENDED ${mCurrentVideoIndex}")
                        if(mCurrentVideoIndex == mVideoPathList.size -1) {
                            doRestartVideo()
                        } else {
                            onNextVideo()
                        }

                    } else {

                    }

                }
            })
        }

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this, "videomaker", bandwidthMeter)
        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.fromFile(File(path)))

        mPlayer?.prepare(mediaSource)
        listenVideoTime()
    }

    private fun listenVideoTime() {
        mTimer = object :CountDownTimer(60*60*1000, 40) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    videoControllerView.setCurrentDuration(mTimelineOffset+(mPlayer?.currentPosition ?: 0))
                }
            }

        }.start()
    }

    private fun doRestartVideo() {
        mTimelineOffset = 0
        mCurrentVideoIndex=0
        changeVideo(mVideoPathList[mCurrentVideoIndex])
        doPlayVideo()
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

                showProgressDialog()

                Thread {
                    VideoMakerApplication.instance.releaseRewardAd()
                    mPlayer?.release()
                    mPlayer = null
                    Thread.sleep(500)
                    runOnUiThread {
                        VideoMakerApplication.instance.releaseRewardAd()
                        dismissProgressDialog()
                        val intent = Intent(this@JoinVideoActivity2, ProcessVideoActivity::class.java)
                        intent.putStringArrayListExtra("joinVideoList", mVideoPathList)
                        intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.joinVideoActon)
                        startActivity(intent)
                        mDoJoin = true
                    }
                }.start()



            } else {
                showToast(getString(R.string.free_space_too_low))
            }


        }

        bgView.setOnClickListener {
            if(mDoJoin) {
                doRestartVideo()
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


                changeVideo(mVideoPathList[mCurrentVideoIndex])
                mPlayer?.seekTo((timeMilSec-time).toLong())
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
                changeVideo(mVideoPathList[mCurrentVideoIndex])
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
        Thread{
            mVideoPathList.clear()
            mVideoPathList.addAll(imageList)

            var totalDuration = 0
            for (item in mVideoPathList) {
                mVideoDataList.add(VideoForJoinDataModel(item))
                totalDuration += (MediaUtils.getVideoDuration(item))
            }
            runOnUiThread {
                setupListView()
                changeVideo(mVideoPathList[0])
                dismissProgressDialog()
                videoControllerView.setMaxDuration(totalDuration)
            }


        }.start()

    }
    private var mCurrentDuration = 0
    private fun setupListView() {
        videoListView.apply {
            adapter = mVideoInJoinerAdapter
            layoutManager =
                LinearLayoutManager(this@JoinVideoActivity2, LinearLayoutManager.HORIZONTAL, false)
        }
        for(item in mVideoDataList) {
            mVideoInJoinerAdapter.addItem(item)
        }
        mVideoInJoinerAdapter.highlightItem(mVideoDataList[0].id)

    }




    private fun onNextVideo() {

        if(mCurrentVideoIndex == mVideoPathList.size-1) {
            onRestartVideo()
            //doSeekTo(0)
        } else {
            ++mCurrentVideoIndex
            updateTimelineOffset()
            changeVideo(mVideoPathList[mCurrentVideoIndex])
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

        mTimer?.cancel()
        mPlayer?.release()
        mPlayer = null
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
        mPlayer?.playWhenReady = false
        mTimer?.cancel()
        icPlay.visibility = View.VISIBLE
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mPlayer?.playWhenReady = true
        mTimer?.start()
        icPlay.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        if(mDoJoin) {
            mDoJoin = false
            showProgressDialog()
            Thread{
                Thread.sleep(500)
                runOnUiThread {
                    dismissProgressDialog()
                    doRestartVideo()
                }
            }.start()

        }
    }

}
