package com.hope_studio.base_ads.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.dialog.DialogRating
import android.os.Build
import com.hope_studio.base_ads.activity.Finish2Activity
import com.hope_studio.base_ads.dialog.LoadingDialog


abstract class BaseActivity : AppCompatActivity() {

    abstract fun getLayoutId(): Int

    var dialogRating: DialogRating? = null

    companion object {
        const val BUNDLE_KEY = "BUNDLE_KEY"
    }

    open fun fullScreencall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun setUpDialogRatting(emailStr: String) {
        dialogRating = DialogRating(this, emailStr)
    }

    fun showDialogWithExit(isExit: Boolean) {
        dialogRating?.setExit(isExit)
        dialogRating?.show()
    }

    override fun onBackPressed() {
        try {
            val dialogLoad = LoadingDialog(this)
            BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun finishAds() {
        val dialogLoad = LoadingDialog(this)
        BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
            finish()
        }
    }

    fun getBaseApplication(): BaseApplication? {
        return application as BaseApplication?
    }

    open fun getBaseBundle(): Bundle? {
        return intent.getBundleExtra(BUNDLE_KEY)
    }

    fun openNewActivity(c: Class<*>, isShowAds: Boolean, isFinish: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    startActivity(intent)
                    if (isFinish) finish()
                }
            } else {
                val intent = Intent(this, c)
                startActivity(intent)
                if (isFinish) finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openExitApp() {
        try {
            val intent = Intent(this, Finish2Activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivityAndClearStack(c: Class<*>, isShowAds: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, c)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivityAndClearStack(c: Class<*>, bundle: Bundle, isShowAds: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(BUNDLE_KEY, bundle)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, c)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(BUNDLE_KEY, bundle)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivity(c: Class<*>?, bundle: Bundle, isShowAds: Boolean, isFinish: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    intent.putExtra(BUNDLE_KEY, bundle)
                    startActivity(intent)
                    if (isFinish) finish()
                }
            } else {
                val intent = Intent(this, c)
                intent.putExtra(BUNDLE_KEY, bundle)
                startActivity(intent)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivity(intent: Intent, isShowAds: Boolean, isFinish: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    startActivity(intent)
                    if (isFinish) finish()
                }
            } else {
                startActivity(intent)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivity(intent: Intent, bundle: Bundle, isShowAds: Boolean, isFinish: Boolean) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    intent.putExtra(BUNDLE_KEY, bundle)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    if (isFinish) finish()
                }
            } else {
                intent.putExtra(BUNDLE_KEY, bundle)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivityForResult(
        c: Class<*>, request_code: Int, isShowAds: Boolean, isFinish: Boolean
    ) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    startActivityForResult(intent, request_code)
                    if (isFinish) finish()
                }
            } else {
                val intent = Intent(this, c)
                startActivityForResult(intent, request_code)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivityForResult(
        c: Class<*>, bundle: Bundle, request_code: Int, isShowAds: Boolean, isFinish: Boolean
    ) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    val intent = Intent(this, c)
                    intent.putExtra(BUNDLE_KEY, bundle)
                    startActivityForResult(intent, request_code)
                    if (isFinish) finish()
                }
            } else {
                val intent = Intent(this, c)
                intent.putExtra(BUNDLE_KEY, bundle)
                startActivityForResult(intent, request_code)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNewActivityForResult(
        intent: Intent, request_code: Int, isShowAds: Boolean, isFinish: Boolean
    ) {
        try {
            if (isShowAds) {
                val dialogLoad = LoadingDialog(this)
                BaseAds.loadAndShowInterstitialAd(this, dialogLoad, 0) {
                    startActivityForResult(intent, request_code)
                    if (isFinish) finish()
                }
            } else {
                startActivityForResult(intent, request_code)
                if (isFinish) finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}