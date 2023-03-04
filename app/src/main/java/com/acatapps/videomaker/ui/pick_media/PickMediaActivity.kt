package com.acatapps.videomaker.ui.pick_media

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.ItemTouchHelperCallback
import com.acatapps.videomaker.adapter.MediaPickedAdapter
import com.acatapps.videomaker.adapter.PickMediaPagerAdapter
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.data.MediaData
import com.acatapps.videomaker.enum_.MediaKind
import com.acatapps.videomaker.enum_.VideoActionKind
import com.acatapps.videomaker.models.MediaDataModel
import com.acatapps.videomaker.models.MediaPickedDataModel
import com.acatapps.videomaker.ui.edit_video.VideoSlideActivity2
import com.acatapps.videomaker.ui.join_video.JoinVideoActivity2
import com.acatapps.videomaker.ui.slide_show_v2.ImageSlideShowActivity
import com.acatapps.videomaker.ui.trim_video.TrimVideoActivity
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import kotlinx.android.synthetic.main.activity_pick_media.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.File
import kotlin.math.roundToInt

class PickMediaActivity : BaseActivity(), KodeinAware {
    override fun getContentResId(): Int = R.layout.activity_pick_media
    private var mMediaKind = MediaKind.PHOTO

    private val mPickMediaViewModelFactory: PickMediaViewModelFactory by instance<PickMediaViewModelFactory>()
    private lateinit var mPickMediaViewModel: PickMediaViewModel

    private var mVideoActionKind = VideoActionKind.SLIDE

    private val mMediaPickedAdapter = MediaPickedAdapter {

        performDeleteItemPicked(it)
        updateNumberImageSelected()
    }
    var flag = false
    companion object {
        const val TAKE_PICTURE = 1001
        const val RECORD_CAMERA = 1991
        const val COLS_IMAGE_LIST_SIZE = 90
        const val COLS_ALBUM_LIST_SIZE = 120
        const val CAMERA_PERMISSION_REQUEST = 1002

        const val ADD_MORE_PHOTO = 1003
        const val ADD_MORE_PHOTO_REQUEST_CODE = 1004

        const val ADD_MORE_VIDEO = 1005
        const val ADD_MORE_VIDEO_REQUEST_CODE = 1006

        fun gotoActivity(activity: Activity, mediaKind: MediaKind) {
            val intent = Intent(activity, PickMediaActivity::class.java).apply {
                putExtra("MediaKind", mediaKind.toString())
            }
            activity.startActivity(intent)
        }

        fun gotoActivity(activity: Activity, mediaKind: MediaKind, themePath:String) {
            val intent = Intent(activity, PickMediaActivity::class.java).apply {
                putExtra("MediaKind", mediaKind.toString())
                putExtra("themePath", themePath)
            }
            activity.startActivity(intent)
        }

        fun gotoActivity(activity: Activity, videoActionKind: VideoActionKind) {
            val intent = Intent(activity, PickMediaActivity::class.java).apply {
                putExtra("MediaKind", MediaKind.VIDEO.toString())
                putExtra("VideoActionKind", videoActionKind.toString())
            }
            activity.startActivity(intent)
        }

    }

    override fun isShowAds(): Boolean {
        return true
    }

    private var mActionCode = -1
    private var mFlag = false
    override val kodein by closestKodein()

    private val mListPhotoPath = ArrayList<String>()
    private var startAvailable = true
    private var mThemeFileName = ""
    override fun initViews() {

        mPickMediaViewModel = ViewModelProvider(this, mPickMediaViewModelFactory).get(PickMediaViewModel::class.java)
        listen()

        val action = intent.getIntExtra("action", -1)
        if(action != ADD_MORE_PHOTO && action != ADD_MORE_VIDEO) {
            VideoMakerApplication.instance.showAdsFull()
        }
        mActionCode = action
        Logger.e("action = $action")
        if(action == ADD_MORE_PHOTO) {
            setScreenTitle(getString(R.string.photo))
             intent.getStringArrayListExtra("list-photo")?.let {
                 for (path in it) {
                     mListPhotoPath.add(path)
                 }
            }
            mMediaKind = MediaKind.PHOTO
        } else if(action == ADD_MORE_VIDEO){
            setScreenTitle(getString(R.string.video))
            intent.getStringArrayListExtra("list-video")?.let {
                mListPhotoPath.addAll(it)
            }
            mMediaKind = MediaKind.VIDEO
        }else {
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
        mThemeFileName = intent.getStringExtra("themePath") ?: ""
        tabLayout.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = PickMediaPagerAdapter(this, supportFragmentManager)

        val col = (DimenUtils.screenWidth(this) / (96 * DimenUtils.density(this))).roundToInt()
        mediaPickedListView.adapter = mMediaPickedAdapter
        mediaPickedListView.layoutManager = GridLayoutManager(this, col.toInt())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

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

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    override fun initActions() {

        setRightButton(R.drawable.ic_camera_vector) {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }



        expandViewButton.setOnClickListener {
            if (isExpanded) collapseView()
            else expandView()
        }

        startButton.setClick {
            if(startAvailable) {
                startAvailable = false
                if (mMediaPickedAdapter.itemCount < 2) {
                    if (mMediaKind == MediaKind.PHOTO)
                        Toast.makeText(this, getString(R.string.select_at_least_2_image), Toast.LENGTH_LONG).show()
                    else {
                        if(mVideoActionKind == VideoActionKind.SLIDE) {
                            if(mMediaPickedAdapter.itemCount > 0) {
                                val items = arrayListOf<String>()
                                for (item in mMediaPickedAdapter.itemList) {
                                    items.add(item.path)
                                }
                                if(mActionCode == ADD_MORE_VIDEO) {
                                    val intent = Intent().apply {
                                        putStringArrayListExtra("Video picked list", items)
                                    }
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                } else {
                                    val intent = Intent(this, VideoSlideActivity2::class.java)
                                    intent.putStringArrayListExtra("Video picked list", items)
                                    startActivity(intent)
                                }

                            } else {
                                Toast.makeText(this, getString(R.string.select_at_least_1_video), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.select_at_least_2_videos), Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    val items = arrayListOf<String>()
                    for (item in mMediaPickedAdapter.itemList) {
                        items.add(item.path)
                    }
                    Logger.e("items size = ${items.size}")
                    if (mMediaKind == MediaKind.PHOTO) {
                        if(mActionCode == ADD_MORE_PHOTO) {
                            val intent = Intent().apply {
                                putStringArrayListExtra(ImageSlideShowActivity.imagePickedListKey, items)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            val intent = Intent(this, ImageSlideShowActivity::class.java)
                            intent.putStringArrayListExtra(ImageSlideShowActivity.imagePickedListKey, items)
                            if(mThemeFileName.isNotEmpty()) intent.putExtra("themeFileName", mThemeFileName)
                            startActivity(intent)
                        }

                    } else {
                        if (mVideoActionKind == VideoActionKind.JOIN) {
                            JoinVideoActivity2.gotoActivity(this, items)
                        } else if(mActionCode == ADD_MORE_VIDEO){
                            val intent = Intent().apply {
                                putStringArrayListExtra("Video picked list", items)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }else {
                            val intent = Intent(this, VideoSlideActivity2::class.java)
                            intent.putStringArrayListExtra("Video picked list", items)
                            startActivity(intent)
                        }

                    }

                }

                object :CountDownTimer(1000,3000) {
                    override fun onFinish() {
                        startAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
    }

    private fun openCamera() {
        if (mMediaKind == MediaKind.VIDEO) {
            recordVideo()
        } else {
            takePhoto()
        }
    }
    private var mMediaCapturePath = ""
    private fun takePhoto() {
        performTakePhoto()
    }
    private fun performTakePhoto() {
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intentCamera.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            val photoUri: Uri
            photoUri = FileProvider.getUriForFile(this, "com.acatapps.videomaker.fileprovider", imageFile)
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intentCamera, TAKE_PICTURE)
        }
    }
    private fun createImageFile(): File {
        val fileName = "image-"
        val file =
            File.createTempFile(
                fileName,
                ".jpg",
                File("${FileUtils.internalPath}/DCIM/Camera")
            )
        mMediaCapturePath = file.absolutePath
        return file
    }
    private fun recordVideo() {
        performRecordCamera()
    }
    private fun performRecordCamera() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, RECORD_CAMERA)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) {
            doSendBroadcast(mMediaCapturePath)
            showProgressDialog()
            object :CountDownTimer(1000, 1000) {
                override fun onFinish() {
                    doAddNewMediaData(mMediaCapturePath)

                    dismissProgressDialog()
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()

            Logger.e("picture path = $mMediaCapturePath")
        }

        if (resultCode == Activity.RESULT_OK && requestCode == RECORD_CAMERA) {
            val uri = data?.data
            Logger.e("video uri = $uri")
            uri?.let {
                val loader = CursorLoader(this, uri, null, null, null, null)
                val cursor = loader.loadInBackground()
                if (cursor != null && cursor.moveToFirst()) {

                    val folderContainName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: ""
                    val dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)) ?: ""
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    Logger.e("file path = $path")
                    val file = File(path)
                    val folderContainId = file.parentFile?.absolutePath ?: ""
                    doSendBroadcast(path)
                    val mediaData = MediaData(dateAdded * 1000, path, file.name, MediaKind.VIDEO, folderContainId, folderContainName, duration)
                    if(mVideoActionKind == VideoActionKind.TRIM) {
                        mPickMediaViewModel.addNewMediaData(mediaData)
                        TrimVideoActivity.gotoActivity(this, path)
                    } else {

                        mPickMediaViewModel.addNewMediaData(mediaData)
                        mPickMediaViewModel.onPickImage(MediaDataModel(mediaData))
                    }


                }
            }

        }
    }

    private fun doAddNewMediaData(filePath: String) {
        val file = File(filePath)
        val date = file.lastModified()
        val fileName = file.name
        val folderId = file.parentFile?.absolutePath ?: ""
        val folderName = file.parentFile?.name ?: ""
        val mediaData = MediaData(date, filePath, fileName, mMediaKind, folderId, folderName, 0)
        mPickMediaViewModel.addNewMediaData(mediaData)
        mPickMediaViewModel.onPickImage(MediaDataModel(mediaData))
    }

    private fun listen() {
        mPickMediaViewModel.itemJustPicked.observe(this, Observer {
            mMediaPickedAdapter.addItem(MediaPickedDataModel(it.filePath))
            imagePickedArea.visibility = View.VISIBLE
            mediaPickedListView.scrollToPosition(mMediaPickedAdapter.itemCount - 1)
            updateNumberImageSelected()
        })


    }

    @SuppressLint("SetTextI18n")
    fun updateNumberImageSelected() {
        val firstText = getString(R.string.selected) +" ("
        val numberText = mMediaPickedAdapter.itemCount.toString()
        val endText = ") "+if (mMediaKind == MediaKind.VIDEO) getString(R.string.video) else getString(R.string.photos)
        val spannable = SpannableString(firstText+numberText+endText)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.orangeA01)), firstText.length, firstText.length+numberText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        requestCameraPermission()
                    } else {
                        openActSetting()
                    }
                }
            }
        }
    }
    protected fun openActSetting() {

        val view = showYesNoDialogForOpenSetting(getString(R.string.you_can_use_this_feature)+"\n"+getString(R.string.goto_setting_and_grant_camera_permission), {
            Logger.e("click Yes")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        },{finishAfterTransition();},{finishAfterTransition();})

    }
    private var isExpanded = false

    override fun onResume() {
        super.onResume()
        mPickMediaViewModel.localStorageData.getAllMedia(mMediaKind)
        mMediaPickedAdapter.checkFile()
        if(mMediaPickedAdapter.itemCount <= 0) {
            imagePickedArea.visibility = View.GONE
        } else {
            updateNumberImageSelected()
        }

    }

    private fun expandView() {
        if (isExpanded) return
        expandViewButton.rotation = 180f
        val target = DimenUtils.screenHeight(this) * 2 / 3 - 220 * DimenUtils.density(this)
        imagePickedArea.layoutParams.height += target.toInt()
        imagePickedArea.requestLayout()
        isExpanded = true
    }

    private fun collapseView() {
        if (!isExpanded) return
        expandViewButton.rotation = 0f
        val target = DimenUtils.screenHeight(this) * 2 / 3 - 220 * DimenUtils.density(this)
        imagePickedArea.layoutParams.height -= target.toInt()
        imagePickedArea.requestLayout()
        isExpanded = false
    }

    private fun checkCameraPermission(): Boolean { //true if GRANTED
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }



    override fun onBackPressed() {
        if(mPickMediaViewModel.folderIsShowing) {
            mPickMediaViewModel.hideFolder()
        } else {
            super.onBackPressed()
        }

    }

}
