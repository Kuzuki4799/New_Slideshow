package com.hope_studio.base_ads.base

import android.content.Context
import android.view.LayoutInflater
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.utils.BLog

abstract class BaseDialog(context: Context, gravity: Int, isCancel: Boolean) {

    abstract fun getLayoutId(): Int

    var dialogPlus: DialogPlus? = null

    init {
        val view = LayoutInflater.from(context).inflate(getLayoutId(), null)
        dialogPlus = DialogPlus.newDialog(context)
            .setGravity(gravity)
            .setCancelable(isCancel)
            .setContentBackgroundResource(R.color.transparent)
            .setContentHolder(ViewHolder(view))
            .create()
        BLog.d("dialog: " + this.javaClass.simpleName)
    }

    open fun <W> initView(id: Int): W {
        return dialogPlus?.findViewById(id) as W
    }

    open fun show() {
        if (dialogPlus != null) dialogPlus?.show()
    }

    open fun dismiss() {
        if (dialogPlus != null) dialogPlus?.dismiss()
    }

    interface OnDialogCallback {
        fun onDialogListener()
    }

    interface OnDialogDataCallback<T> {
        fun onDialogListener(value: T)
    }

    interface OnDialogDataPositionCallback{
        fun onDialogListener(position: Int)
    }

    interface OnDialogObjectCallback<T> {
        fun onDialogListener(value: T, dialogPlus: DialogPlus?)
    }
}