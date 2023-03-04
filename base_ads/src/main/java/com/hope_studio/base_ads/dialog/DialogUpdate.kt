package com.hope_studio.base_ads.dialog

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.base.BaseDialog
import com.hope_studio.base_ads.utils.NetWorkUtils

class DialogUpdate(val url: String, val activity: BaseActivity) :
    BaseDialog(activity, Gravity.CENTER, false) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_update
    }

    init {
        val txtMessage = (dialogPlus?.findViewById(R.id.message) as AppCompatTextView)
        val txtYes = (dialogPlus?.findViewById(R.id.txtYes) as AppCompatTextView)

        val appName = activity.getString(R.string.app_name)
        txtMessage.text = activity.getString(R.string.update_version_content, appName)

        txtYes.setOnClickListener {
            if (url.isNotEmpty()) {
                NetWorkUtils.intentToChPlay(activity, url)
            }
        }
    }
}