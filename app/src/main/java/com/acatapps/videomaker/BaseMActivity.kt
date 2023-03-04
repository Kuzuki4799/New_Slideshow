package com.acatapps.videomaker

import android.animation.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.acatapps.videomaker.application.VideoMakerApplication
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.acatapps.videomaker.data.ThemeLinkData
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.Utils
import com.hope_studio.base_ads.activity.BaseMainActivity
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.base.BaseActivity
import kotlinx.android.synthetic.main.activity_base_layout.*
import kotlinx.android.synthetic.main.base_header_view.*
import kotlinx.android.synthetic.main.base_header_view.view.*
import kotlinx.android.synthetic.main.layout_download_theme_dialog.view.*
import kotlinx.android.synthetic.main.layout_yes_no_dialog.view.*
import java.io.File
import kotlin.math.roundToInt

abstract class BaseMActivity : BaseMainActivity() {

    private var mProgressIsShowing = false
    protected var mYesNoDialogShowing = false
    protected var mRateDialogShowing = false
    var needShowDialog = false
    var comebackStatus = ""
    var mRateAvailable = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_base_layout)

        comebackStatus = getString(R.string.do_you_want_to_come_back)
        mainContentLayout.apply {
            removeAllViews()
            addView(View.inflate(context, getLayoutId(), null))
        }
        showHeader()

        headerView.screenTitle.text = screenTitle()

        icBack.setOnClickListener {
            hideKeyboard()
            onBackPressed()
        }

        initViews()
        initActions()

        checkUpdate()
        Handler().postDelayed({ setUpDialogRatting(emailStr()) }, 2000)
        showAds()
    }


    private fun showAds() {
        adView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                adView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                BaseAds.loadBaseNativeOrBanner(
                    this@BaseMActivity, 0, adView,
                    frame_native_banner, adView.width
                )
            }
        })
    }

    protected fun checkSettingAutoUpdateTime(): Boolean {
        val i1 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME)
        val i2 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME_ZONE)
        Logger.e("i1 = $i1 --- i2 = $i2")
        if (i1 == 1 && i2 == 1) return true
        return false
    }

    protected fun setScreenTitle(title: String) {
        headerView.screenTitle.text = title
    }

    private fun showHeader() {
        headerView.visibility = View.VISIBLE
    }

    protected fun hideHeader() {
        headerView.visibility = View.GONE
    }

    open fun screenTitle(): String = ""

    fun setRightButton(drawableId: Int? = null, onClick: () -> Unit) {
        drawableId?.let {
            rightButton.setImageResource(it)
            rightButton.visibility = View.VISIBLE
            rightButton.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    protected fun showProgressDialog() {
        if (!mProgressIsShowing) {
            LayoutInflater.from(this).inflate(R.layout.layout_progress_dialog, baseRootView, true)
            mProgressIsShowing = true
        }
    }

    protected fun dismissProgressDialog() {
        if (mProgressIsShowing) {
            baseRootView.removeViewAt(baseRootView.childCount - 1)
            mProgressIsShowing = false
        }
    }

    protected fun showYesNoDialogForOpenSetting(
        title: String,
        onClickYes: () -> Unit, onClickNo: (() -> Unit)? = null, onClickBg: (() -> Unit)? = null
    ) {
        if (mYesNoDialogShowing) return

        val view =
            LayoutInflater.from(this).inflate(R.layout.layout_yes_no_dialog, baseRootView, true)
        view.dialogTitle.text = title
        view.yesButton.text = getString(R.string.setting)
        view.dialogTitle.setTextColor(Color.parseColor("#73000000"))
        view.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        view.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        view.bgBlackOnYesNo.setOnClickListener {
        }

        scaleAnimation(view.dialogContentOnYesNo)
        alphaInAnimation(view.bgBlackOnYesNo)
        mYesNoDialogShowing = true
        return
    }

    private fun dismissYesNoDialog() {
        if (mYesNoDialogShowing) {
            baseRootView.removeViewAt(baseRootView.childCount - 1)
            mYesNoDialogShowing = false
        }
    }

    abstract fun initViews()
    abstract fun initActions()

    private fun scaleAnimation(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    private fun alphaInAnimation(view: View) {
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, alpha).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    fun doSendBroadcast(filePath: String) {
        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(File(filePath))
            )
        )
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(baseRootView.applicationWindowToken, 0)
    }

    private val downloadViewHashMap = HashMap<String, View?>()
    var mDownloadDialogIsShow = false
    fun showDownloadThemeDialog(
        linkData: ThemeLinkData,
        onClickDone: () -> Unit,
        onDownloadComplete: () -> Unit
    ) {
        if (mDownloadDialogIsShow) return
        mDownloadDialogIsShow = true
        val view = if (downloadViewHashMap[linkData.link] != null) {
            downloadViewHashMap[linkData.link]!!
        } else {
            LayoutInflater.from(this).inflate(R.layout.layout_download_theme_dialog, null, false)
        }
        baseRootView.addView(view)
        if (downloadViewHashMap[linkData.link] == null) {
            downloadViewHashMap[linkData.link] = view

            view.themeNameLabel.text = linkData.name
            val uriString = "file:///android_asset/theme-icon/${linkData.fileName}.jpg"
            Glide.with(this)
                .load(Uri.parse(uriString))
                .into(view.themeIconInDownloadDialog)

            view.blackViewInDownloadThemeDialog.setOnClickListener {

            }
            view.icClose.setOnClickListener {
                dismissDownloadDialog()
            }
            view.doneButton.setOnClickListener {
                dismissDownloadDialog()
                onClickDone.invoke()
            }

            view.tryAgainButton.setOnClickListener {
                view.tryAgainButton.visibility = View.INVISIBLE
                view.downloadingViewContainer.visibility = View.VISIBLE
                onDownloadTheme(linkData.link, linkData.fileName, onDownloadComplete, view)
            }

            view.watchVideoButton.setOnClickListener {

//                Logger.e("load ad")
//                showProgressDialog()
//                VideoMakerApplication.instance.loadAdFullForTheme {
//                    runOnUiThread {
//                        view.watchVideoButton.visibility = View.GONE
//                        view.downloadingViewContainer.visibility = View.VISIBLE
//                        dismissProgressDialog()
//                    }
//                    onDownloadTheme(linkData.link, linkData.fileName, onDownloadComplete, view)
//                }
            }
        }

        scaleAnimation(view.downloadThemeDialogContent)
        alphaInAnimation(view.blackViewInDownloadThemeDialog)
    }

    private fun onDownloadTheme(
        link: String, fileName: String, onComplete: () -> Unit, view: View
    ) {
        PRDownloader.download(link, FileUtils.themeFolderPath, "${fileName}.mp4")
            .build()
            .setOnProgressListener {
                Logger.e("progess = $it")
                val progress = it.currentBytes.toFloat() / it.totalBytes
                Logger.e("progress = ${(progress * 100f).roundToInt()}")
                if (mDownloadDialogIsShow)
                    runOnUiThread {
                        view.downloadingProgressBar?.setProgress((progress * 100f).roundToInt())
                    }

            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {

                    view.doneButton?.visibility = View.VISIBLE
                    view.tryAgainButton?.visibility = View.INVISIBLE
                    view.downloadingViewContainer.visibility = View.GONE
                    downloadViewHashMap.remove(link)
                    onComplete.invoke()
                }

                override fun onError(error: Error?) {
                    Logger.e("download error --> ${error?.connectionException?.message}")
                    runOnUiThread {
                        view.tryAgainButton?.visibility = View.VISIBLE
                        view.downloadingViewContainer.visibility = View.GONE
                    }
                    Thread {
                        if (!Utils.isInternetAvailable()) {
                            runOnUiThread {
                                showToast(getString(R.string.no_internet_connection_please_connect_to_the_internet_and_try_again))
                            }

                        }
                    }.start()

                }
            })
    }

    fun dismissDownloadDialog() {
        if (mDownloadDialogIsShow) {
            baseRootView.removeViewAt(baseRootView.childCount - 1)
            mDownloadDialogIsShow = false
        }
    }
}