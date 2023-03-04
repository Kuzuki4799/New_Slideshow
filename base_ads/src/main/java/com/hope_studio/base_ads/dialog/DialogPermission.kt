package com.hope_studio.base_ads.dialog

import android.app.Activity
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseDialog

class DialogPermission(
    activity: Activity, private val onHandlerEventListener: OnDialogCallback
) : BaseDialog(activity, Gravity.CENTER, false), View.OnClickListener {

    override fun getLayoutId(): Int {
        return R.layout.dialog_permission
    }

    init {
        val btnAccept = dialogPlus?.findViewById(R.id.btnAccept) as AppCompatTextView
        val btnClose = dialogPlus?.findViewById(R.id.btnClose) as AppCompatTextView
        btnAccept.setOnClickListener(this)
        btnClose.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> handlerClose()
            R.id.btnAccept -> handlerAccept()
        }
    }

    private fun handlerAccept() {
        onHandlerEventListener.onDialogListener()
        dismiss()
    }

    private fun handlerClose() {
        dismiss()
    }
}