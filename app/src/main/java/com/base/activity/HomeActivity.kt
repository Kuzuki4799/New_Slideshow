package com.base.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.CountDownTimer
import android.provider.Settings
import android.view.ViewTreeObserver
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.video_maker_slideshow_maker.BuildConfig
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_enum_.MediaKind
import com.hope_studio.video_maker_slideshow_maker.ho_enum_.VideoActionKind
import com.hope_studio.video_maker_slideshow_maker.ho_ui.ho_pick_media.PickMediaActivity
import com.hope_studio.video_maker_slideshow_maker.ho_utils.*
import com.hope_studio.base_ads.dialog.DialogRating
import com.hope_studio.video_maker_slideshow_maker.ho_ui.MyStudioActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

open class HomeActivity : BaseMActivity() {

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 1002
        const val STORAGE_PERMISSION_REQUEST = 1003
    }

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

        Logger.e("check storage permission in on create = ${checkStoragePermission()}")
        if (!checkStoragePermission()) {
            requestStoragePermission()
        }

        llNative.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                llNative.viewTreeObserver.removeOnGlobalLayoutListener(this)
                BaseAds.loadBaseNativeAd(
                    this@HomeActivity,
                    0, nativeAdViewInProcess, llNative.width
                )
            }
        })
    }

    private fun onInit() {
        onSplashComplete = true
        needShowDialog = true

        if (checkStoragePermission()) {
            Thread {
                try {
                    initDefaultAudio()
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

    override fun initActions() {
        bgButtonSlideShow.setOnClickListener {
            gotoPickMedia(MediaKind.PHOTO)
        }
        bgButtonEditVideo.setOnClickListener {
            if (pickMediaAvailable) {
                pickMediaAvailable = false
                openNewActivity(MyStudioActivity::class.java, isShowAds = true, isFinish = false)
                countDownAvailable()
            }
        }
        bgTrimVideo.setOnClickListener {
            gotoPickMedia(VideoActionKind.TRIM)
        }
        bgJoinVideo.setOnClickListener {
            gotoPickMedia(VideoActionKind.JOIN)
        }

        bgRating.setOnClickListener {
            DialogRating(this, BuildConfig.EMAIL).show()
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

    private var mOnPause = false

    override fun onPause() {
        super.onPause()
        mOnPause = true
    }

    override fun onResume() {
        super.onResume()
        if (checkStoragePermission()) {
            onInit()
        }
        if (showSetting && !checkStoragePermission()) {
            showSetting = false
            openActSetting()
        }
    }
}
