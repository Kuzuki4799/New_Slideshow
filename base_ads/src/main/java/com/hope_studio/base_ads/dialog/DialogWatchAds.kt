package com.hope_studio.base_ads.dialog

import android.app.Activity
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseDialog

class DialogWatchAds(
    val activity: Activity, str: String, private val onDialogCallback: OnDialogCallback
) : BaseDialog(activity, Gravity.CENTER, false), View.OnClickListener {

    init {
        val txtNo = (dialogPlus?.findViewById(R.id.txtNo) as AppCompatTextView)
        val txtYes = (dialogPlus?.findViewById(R.id.txtYes) as AppCompatTextView)
        val message = (dialogPlus?.findViewById(R.id.message) as AppCompatTextView)

        message.text = activity.getString(R.string.watching_video_, str)

        txtNo.setOnClickListener(this)
        txtYes.setOnClickListener(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_watch_ads
    }

    override fun onClick(v: View?) {
        dialogPlus?.dismiss()
        when (v?.id) {
            R.id.txtYes -> onDialogCallback.onDialogListener()
        }
    }
}