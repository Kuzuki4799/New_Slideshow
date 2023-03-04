package com.hope_studio.base_ads.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.model.DataModel

object Utils {

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isInputKeyboardEnabled(activity: Activity): Boolean {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val mInputMethodProperties = imm.enabledInputMethodList
        val N = mInputMethodProperties.size
        var isInputEnabled = false
        for (i in 0 until N) {
            val imi = mInputMethodProperties[i]
            Log.d("INPUT ID", imi.id.toString())
            if (imi.id.contains(activity.packageName)) {
                isInputEnabled = true
            }
        }
        return isInputEnabled
    }

    private fun getDeviceInfo(): String {
        return (Build.MODEL + " " + Build.BRAND + " ("
                + Build.VERSION.RELEASE + ")" + " API-" + Build.VERSION.SDK_INT)
    }

    fun handleSendEmail(activity: Activity, emailStr: String, rating: Int, feedback: String) {
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        val email = if (dataModel == null) {
            emailStr
        } else {
            if (dataModel.getEmailApp().isNotEmpty()) dataModel.getEmailApp()
            else emailStr
        }
        val packageGmail = "com.google.android.gm"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        if (isPackageInstalled(packageGmail, activity.packageManager)) {
            intent.setPackage(packageGmail)
        }
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            activity.getString(R.string.app_ratting, activity.getString(R.string.app_name))
        )
        intent.putExtra(
            Intent.EXTRA_TEXT,
            activity.getString(
                R.string.content_feedback, getDeviceInfo(), activity.getString(R.string.app_name),
                NetWorkUtils.getLinkStore(activity), rating, feedback
            )
        )
        ShareUtils.putBoolean(activity, "rate", true)
        if (isPackageInstalled(packageGmail, activity.packageManager)) {
            activity.startActivity(intent)
        } else {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.send_mail))
            )
        }
    }

    fun handleSendEmail2(activity: Activity, emailStr: String) {
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        val email = if (dataModel == null) {
            emailStr
        } else {
            if (dataModel.getEmailApp().isNotEmpty()) dataModel.getEmailApp()
            else emailStr
        }
        val packageGmail = "com.google.android.gm"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        if (isPackageInstalled(packageGmail, activity.packageManager)) {
            intent.setPackage(packageGmail)
        }
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            activity.getString(R.string.app_ratting, activity.getString(R.string.app_name))
        )
        intent.putExtra(
            Intent.EXTRA_TEXT,
            activity.getString(
                R.string.content_feedback_2, getDeviceInfo(), activity.getString(R.string.app_name),
                NetWorkUtils.getLinkStore(activity)
            )
        )
        ShareUtils.putBoolean(activity, "rate", true)
        if (isPackageInstalled(packageGmail, activity.packageManager)) {
            activity.startActivity(intent)
        } else {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.send_mail))
            )
        }
    }
}