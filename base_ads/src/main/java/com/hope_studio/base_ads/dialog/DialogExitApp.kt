package com.hope_studio.base_ads.dialog

import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.base.BaseDialog
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.ads.widget.NativeAds

class DialogExitApp(val activity: BaseActivity) :
    BaseDialog(activity, Gravity.CENTER, false), View.OnClickListener {

    override fun getLayoutId(): Int {
        return R.layout.dialog_exit_app
    }

    init {
        val txtNo = (dialogPlus?.findViewById(R.id.txtNo) as AppCompatTextView)
        val txtYes = (dialogPlus?.findViewById(R.id.txtYes) as AppCompatTextView)
        val myTemplateAd = (dialogPlus?.findViewById(R.id.my_template_medium) as NativeAds)
        val llNative = (dialogPlus?.findViewById(R.id.llNative) as LinearLayout)
        txtNo.setOnClickListener(this)
        txtYes.setOnClickListener(this)
        llNative.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                llNative.viewTreeObserver.removeOnGlobalLayoutListener(this)
                BaseAds.loadBaseNativeAd(activity, 0, myTemplateAd, llNative.width)
            }
        })
    }

    override fun onClick(v: View?) {
        dialogPlus?.dismiss()
        when (v?.id) {
            R.id.txtYes -> BaseAds.showInterstitialAdExit(activity)
        }
    }
}