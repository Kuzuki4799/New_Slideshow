package com.acatapps.videomaker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.acatapps.videomaker.BaseMActivity
import com.acatapps.videomaker.BuildConfig
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.MyStudioInHomeAdapter
import com.acatapps.videomaker.enum_.MediaKind
import com.acatapps.videomaker.enum_.VideoActionKind
import com.acatapps.videomaker.models.MyStudioDataModel
import com.acatapps.videomaker.modules.share.Share
import com.acatapps.videomaker.ui.my_studio.MyStudioActivity
import com.acatapps.videomaker.ui.pick_media.PickMediaActivity
import com.acatapps.videomaker.ui.share_video.ShareVideoActivity
import com.acatapps.videomaker.utils.*
import com.hope_studio.base_ads.dialog.DialogRating
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

open class HomeActivity : BaseMActivity() {

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 1002
        const val STORAGE_PERMISSION_REQUEST = 1003
    }

    private val mMyStudioAdapter = MyStudioInHomeAdapter()

    private var onSplashComplete = false

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun versionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun emailStr(): String {
        return BuildConfig.EMAIL
    }

    override fun initViews() {
        comebackStatus = getString(R.string.do_you_want_to_leave)
        hideHeader()

        myStudioListView.apply {
            adapter = mMyStudioAdapter
            layoutManager =
                LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
        }
        Logger.e("check storage permission in on create = ${checkStoragePermission()}")
        if (!checkStoragePermission()) {
            requestStoragePermission()
        }

    }

    private fun onInit() {
        onSplashComplete = true
        needShowDialog = true

        if (checkStoragePermission()) {
            Thread {
                try {
                    initDefaultAudio()
                    getAllMyStudioItem()
                    FileUtils.clearTemp()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun requestStoragePermission() {
        Logger.e("request permission ")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST
        )
    }

    private val mShare = Share()

    override fun initActions() {
        bgButtonSlideShow.setOnClickListener {
            gotoPickMedia(MediaKind.PHOTO)
        }
        bgButtonEditVideo.setOnClickListener {
            gotoPickMedia(MediaKind.VIDEO)
        }
        bgTrimVideo.setClick {
            gotoPickMedia(VideoActionKind.TRIM)
        }
        bgJoinVideo.setClick {
            gotoPickMedia(VideoActionKind.JOIN)
        }

        bgRating.setClick {
            DialogRating(this, BuildConfig.EMAIL).show()
        }

        bgShare.setClick {
            if (pickMediaAvailable) {
                pickMediaAvailable = false
                mShare.shareApp(this, BuildConfig.APPLICATION_ID)
                countDownAvailable()
            }
        }

        buttonMore.setClick {
            if (pickMediaAvailable) {
                pickMediaAvailable = false
                openNewActivity(MyStudioActivity::class.java, isShowAds = true, isFinish = false)
                countDownAvailable()
            }
        }

        mMyStudioAdapter.onClickItem = {
            ShareVideoActivity.gotoActivity(this, it.filePath)
        }
    }

    private fun countDownAvailable() {
        object : CountDownTimer(1000, 1000) {
            override fun onFinish() {
                pickMediaAvailable = true
            }

            override fun onTick(millisUntilFinished: Long) {

            }
        }.start()
    }

    private fun gotoPickMedia(actionKind: VideoActionKind) {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        if (Utils.getAvailableSpaceInMB() < 100) {
            showToast(getString(R.string.free_space_too_low))
            return
        }
        if (pickMediaAvailable) {
            pickMediaAvailable = false
            PickMediaActivity.gotoActivity(this, actionKind)
            countDownAvailable()
        }
    }

    private var pickMediaAvailable = true

    private fun gotoPickMedia(mediaKind: MediaKind) {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        if (Utils.getAvailableSpaceInMB() < 200) {
            showToast(getString(R.string.free_space_too_low))
            return
        }
        if (pickMediaAvailable) {
            pickMediaAvailable = false
            PickMediaActivity.gotoActivity(this, mediaKind)
            countDownAvailable()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            return
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    onInit()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        requestStoragePermission()
                    } else {
                        openActSetting()
                    }
                }
            } else {
                openActSetting()
            }
            return
        }
    }

    private var showSetting = false

    private fun openActSetting() {
        showYesNoDialogForOpenSetting(
            getString(R.string.anser_grant_permission) + "\n" + getString(R.string.goto_setting_and_grant_permission),
            {
                Logger.e("click Yes")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                showToast(getString(R.string.please_grant_read_external_storage))
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                showSetting = true
            },
            { finishAfterTransition(); },
            { finishAfterTransition(); })

    }

    private fun initDefaultAudio() {
        val audioFolder = File(FileUtils.audioDefaultFolderPath)
        if (!audioFolder.exists()) {
            audioFolder.mkdirs()
        }
        copyDefaultAudio()
    }

    private fun copyDefaultAudio() {
        val fileInAsset = assets.list("audio")
        fileInAsset?.let {
            for (fileName in fileInAsset) {
                val fileOut = File("${FileUtils.audioDefaultFolderPath}/$fileName")
                if (!fileOut.exists()) {
                    val inputStream = assets.open("audio/$fileName")
                    val outputStream = FileOutputStream(fileOut)
                    copyFile(inputStream, outputStream)
                }
            }
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.close()
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAllMyStudioItem() {
        Thread {
            val folder = File(FileUtils.myStuioFolderPath)
            val myStudioDataList = ArrayList<MyStudioDataModel>()
            if (folder.exists() && folder.isDirectory) {
                for (item in folder.listFiles()!!) {
                    try {
                        val duration = MediaUtils.getVideoDuration(item.absolutePath)
                        myStudioDataList.add(
                            MyStudioDataModel(
                                item.absolutePath,
                                item.lastModified(),
                                duration
                            )
                        )
                    } catch (e: Exception) {
                        item.delete()
                        doSendBroadcast(item.absolutePath)
                        continue
                    }
                }
            }

            runOnUiThread {
                if (myStudioDataList.size > 5) {
                    mMyStudioAdapter.clear()
                    for (i in 0 until 5) {
                        mMyStudioAdapter.addItem(myStudioDataList[i])
                    }
                } else {
                    mMyStudioAdapter.setItemList(myStudioDataList)
                }
                if (mMyStudioAdapter.itemCount < 1) {
                    icNoProject.visibility = View.VISIBLE
                    buttonMore.visibility = View.GONE
                } else {
                    icNoProject.visibility = View.GONE
                    buttonMore.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private var mOnPause = false

    override fun onPause() {
        super.onPause()
        mOnPause = true
    }

    override fun onResume() {
        super.onResume()
        if (checkStoragePermission()) {
            getAllMyStudioItem()
            onInit()
        }
        if (showSetting && !checkStoragePermission()) {
            showSetting = false
            openActSetting()
        }
    }
}
