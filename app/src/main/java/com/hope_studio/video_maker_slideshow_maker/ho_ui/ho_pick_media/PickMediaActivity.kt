package com.hope_studio.video_maker_slideshow_maker.ho_ui.ho_pick_media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_adapter.ItemTouchHelperCallback
import com.hope_studio.video_maker_slideshow_maker.ho_adapter.MediaPickedAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_adapter.PickMediaPagerAdapter
import com.hope_studio.video_maker_slideshow_maker.ho_base.BaseActivity
import com.hope_studio.video_maker_slideshow_maker.ho_enum_.MediaKind
import com.hope_studio.video_maker_slideshow_maker.ho_enum_.VideoActionKind
import com.hope_studio.video_maker_slideshow_maker.ho_models.MediaPickedDataModel
import com.hope_studio.video_maker_slideshow_maker.ho_ui.VideoSlideActivity2
import com.hope_studio.video_maker_slideshow_maker.ho_ui.JoinVideoActivity2
import com.hope_studio.video_maker_slideshow_maker.ho_ui.ImageSlideShowActivity
import com.hope_studio.video_maker_slideshow_maker.ho_utils.Logger
import kotlinx.android.synthetic.main.activity_pick_media.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

open class PickMediaActivity : BaseActivity(), KodeinAware {

    override fun getLayoutId(): Int = R.layout.activity_pick_media

    private var mMediaKind = MediaKind.PHOTO

    private val mPickMediaViewModelFactory: PickMediaViewModelFactory by instance()
    private lateinit var mPickMediaViewModel: PickMediaViewModel

    private var mVideoActionKind = VideoActionKind.SLIDE

    private val mMediaPickedAdapter = MediaPickedAdapter {
        performDeleteItemPicked(it)
        updateNumberImageSelected()
    }

    companion object {
        const val COLS_IMAGE_LIST_SIZE = 90
        const val COLS_ALBUM_LIST_SIZE = 120

        const val ADD_MORE_PHOTO = 1003
        const val ADD_MORE_PHOTO_REQUEST_CODE = 1004

        const val ADD_MORE_VIDEO = 1005
        const val ADD_MORE_VIDEO_REQUEST_CODE = 1006

        fun gotoActivity(activity: Activity, mediaKind: MediaKind) {
            val intent = Intent(activity, PickMediaActivity::class.java).apply {
                putExtra("MediaKind", mediaKind.toString())
            }
            (activity as com.hope_studio.base_ads.base.BaseActivity).openNewActivity(
                intent, isShowAds = true, isFinish = false
            )
        }

        fun gotoActivity(activity: Activity, videoActionKind: VideoActionKind) {
            val intent = Intent(activity, PickMediaActivity::class.java).apply {
                putExtra("MediaKind", MediaKind.VIDEO.toString())
                putExtra("VideoActionKind", videoActionKind.toString())
            }
            (activity as com.hope_studio.base_ads.base.BaseActivity).openNewActivity(
                intent, isShowAds = true, isFinish = false
            )
        }
    }

    override fun isShowAds(): Boolean {
        return true
    }

    private var mActionCode = -1
    override val kodein by closestKodein()

    private val mListPhotoPath = ArrayList<String>()
    private var startAvailable = true
    private var mThemeFileName = ""

    override fun initViews() {

        mPickMediaViewModel =
            ViewModelProvider(this, mPickMediaViewModelFactory).get(PickMediaViewModel::class.java)
        listen()

        val action = intent.getIntExtra("action", -1)
        mActionCode = action
        Logger.e("action = $action")
        when (action) {
            ADD_MORE_PHOTO -> {
                setScreenTitle(getString(R.string.photo))
                intent.getStringArrayListExtra("list-photo")?.let {
                    for (path in it) {
                        mListPhotoPath.add(path)
                    }
                }
                mMediaKind = MediaKind.PHOTO
            }
            ADD_MORE_VIDEO -> {
                setScreenTitle(getString(R.string.video))
                intent.getStringArrayListExtra("list-video")?.let {
                    mListPhotoPath.addAll(it)
                }
                mMediaKind = MediaKind.VIDEO
            }
            else -> {
                if (intent.getStringExtra("MediaKind") == MediaKind.VIDEO.toString())
                    mMediaKind = MediaKind.VIDEO
                val actionKind = intent.getStringExtra("VideoActionKind")

                when (mMediaKind) {
                    MediaKind.VIDEO -> {
                        setScreenTitle(getString(R.string.video))
                        actionKind?.let {
                            mVideoActionKind = VideoActionKind.valueOf(it)
                            if (mVideoActionKind == VideoActionKind.TRIM) {
                                mPickMediaViewModel.disableCounter()
                                imagePickedArea.visibility = View.GONE
                            }

                        }
                    }
                    MediaKind.PHOTO -> {
                        setScreenTitle(getString(R.string.photo))
                    }
                }
            }
        }
        mThemeFileName = intent.getStringExtra("themePath") ?: ""
        tabLayout.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = PickMediaPagerAdapter(this, supportFragmentManager)
        mediaPickedListView.adapter = mMediaPickedAdapter
        mediaPickedListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        addItemTouchCallback(mediaPickedListView)
        imagePickedArea.visibility = View.GONE
        mPickMediaViewModel.localStorageData.getAllMedia(mMediaKind)

        for (path in mListPhotoPath) {
            mMediaPickedAdapter.addItem(MediaPickedDataModel(path))
            imagePickedArea.visibility = View.VISIBLE
            mediaPickedListView.scrollToPosition(mMediaPickedAdapter.itemCount - 1)
            updateNumberImageSelected()
        }

    }

    private fun addItemTouchCallback(recyclerView: RecyclerView) {
        val callback = ItemTouchHelperCallback(object : MediaPickedAdapter.ItemTouchListenner {
            override fun onItemMove(fromPosition: Int, toPosition: Int) {
                mMediaPickedAdapter.onMove(fromPosition, toPosition)
            }
        })
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        mMediaPickedAdapter.registerItemTouch(itemTouchHelper)
    }

    override fun initActions() {
        startButton.setClick {
            if (startAvailable) {
                startAvailable = false
                if (mMediaPickedAdapter.itemCount < 2) {
                    if (mMediaKind == MediaKind.PHOTO)
                        Toast.makeText(
                            this,
                            getString(R.string.select_at_least_2_image),
                            Toast.LENGTH_LONG
                        ).show()
                    else {
                        if (mVideoActionKind == VideoActionKind.SLIDE) {
                            if (mMediaPickedAdapter.itemCount > 0) {
                                val items = arrayListOf<String>()
                                for (item in mMediaPickedAdapter.itemList) {
                                    items.add(item.path)
                                }
                                if (mActionCode == ADD_MORE_VIDEO) {
                                    val intent = Intent().apply {
                                        putStringArrayListExtra("Video picked list", items)
                                    }
                                    setResult(Activity.RESULT_OK, intent)
                                    finishAds()
                                } else {
                                    val intent = Intent(this, VideoSlideActivity2::class.java)
                                    intent.putStringArrayListExtra("Video picked list", items)
                                    openNewActivity(intent, isShowAds = true, isFinish = false)
                                }

                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.select_at_least_1_video),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.select_at_least_2_videos),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } else {
                    val items = arrayListOf<String>()
                    for (item in mMediaPickedAdapter.itemList) {
                        items.add(item.path)
                    }
                    Logger.e("items size = ${items.size}")
                    if (mMediaKind == MediaKind.PHOTO) {
                        if (mActionCode == ADD_MORE_PHOTO) {
                            val intent = Intent().apply {
                                putStringArrayListExtra(
                                    ImageSlideShowActivity.imagePickedListKey, items
                                )
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finishAds()
                        } else {
                            val intent = Intent(this, ImageSlideShowActivity::class.java)
                            intent.putStringArrayListExtra(
                                ImageSlideShowActivity.imagePickedListKey, items
                            )
                            if (mThemeFileName.isNotEmpty()) intent.putExtra(
                                "themeFileName",
                                mThemeFileName
                            )
                            openNewActivity(intent, true, isFinish = false)
                        }

                    } else {
                        if (mVideoActionKind == VideoActionKind.JOIN) {
                            JoinVideoActivity2.gotoActivity(this, items)
                        } else if (mActionCode == ADD_MORE_VIDEO) {
                            val intent = Intent().apply {
                                putStringArrayListExtra("Video picked list", items)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finishAds()
                        } else {
                            val intent = Intent(this, VideoSlideActivity2::class.java)
                            intent.putStringArrayListExtra("Video picked list", items)
                            openNewActivity(intent, isShowAds = true, isFinish = false)
                        }
                    }
                }

                object : CountDownTimer(1000, 3000) {
                    override fun onFinish() {
                        startAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
    }

    private fun listen() {
        mPickMediaViewModel.itemJustPicked.observe(this) {
            mMediaPickedAdapter.addItem(MediaPickedDataModel(it.filePath))
            imagePickedArea.visibility = View.VISIBLE
            mediaPickedListView.scrollToPosition(mMediaPickedAdapter.itemCount - 1)
            updateNumberImageSelected()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateNumberImageSelected() {
        val firstText = getString(R.string.selected) + " ("
        val numberText = mMediaPickedAdapter.itemCount.toString()
        val endText =
            ") " + if (mMediaKind == MediaKind.VIDEO) getString(R.string.video) else getString(R.string.photos)
        val spannable = SpannableString(firstText + numberText + endText)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.orangeA01)),
            firstText.length, firstText.length + numberText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        numberMediaPicked.text = spannable
    }

    private fun performDeleteItemPicked(position: Int) {
        mPickMediaViewModel.onDelete(mMediaPickedAdapter.itemList[position])
        mMediaPickedAdapter.itemList.removeAt(position)
        mMediaPickedAdapter.notifyDataSetChanged()

        if (mMediaPickedAdapter.itemCount < 1) {
            imagePickedArea.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        mPickMediaViewModel.localStorageData.getAllMedia(mMediaKind)
        mMediaPickedAdapter.checkFile()
        if (mMediaPickedAdapter.itemCount <= 0) {
            imagePickedArea.visibility = View.GONE
        } else {
            updateNumberImageSelected()
        }
    }

    override fun onBackPressed() {
        if (mPickMediaViewModel.folderIsShowing) {
            mPickMediaViewModel.hideFolder()
        } else {
            super.onBackPressed()
        }

    }
}
