package com.hope_studio.base_ads.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Toast
import com.hope_studio.base_ads.model.DataModel
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


object NetWorkUtils {

    fun shareUrl(context: Context, url: String) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
        share.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(Intent.createChooser(share, "Share"))
    }

    fun shareApp(context: Context) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        share.putExtra(Intent.EXTRA_SUBJECT, "Share link")
        share.putExtra(Intent.EXTRA_TEXT, getLinkStore(context))
        context.startActivity(Intent.createChooser(share, "Share"))
    }

    fun intentToChPlay(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getLinkStore(context))))
    }

    fun getLinkStore(context: Context): String {
        return "https://play.google.com/store/apps/details?id=${context.packageName}"
    }

    fun getLinkMarket(context: Context): String {
        return "market://details?id=${context.packageName}"
    }

    fun checkLinkMarket(context: Context, url: String): Boolean {
        return url == getLinkStore(context) || url == getLinkMarket(context)
    }

    fun intentToChPlay(context: Context, packageName: String) {
        if (packageName.isEmpty()) return
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageName)))
    }

    @SuppressLint("DefaultLocale")
    fun ipWifiAddress(context: Context): String {
        val ipString: String
        val wifiMgr =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiMgr.connectionInfo
        val ip = wifiInfo.ipAddress
        ipString = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
        return ipString
    }

    private fun urlEncode(s: String): String {
        return try {
            URLEncoder.encode(s, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Log.wtf("urlEncode", "UTF-8 should always be supported", e)
            ""
        }
    }

    fun handlerMoreApp(context: Context) {
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return
        val uri = Uri.parse(dataAds.getLinkMoreApps())
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show()
        }
    }

    fun intentPermissionSettingDevice(context: Context) {
        context.startActivity(Intent().apply {
            action = ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        })
    }

    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun isWifiConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isConnected
            }
        }
        return false
    }

    fun isMobileConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mMobileNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isConnected
            }
        }
        return false
    }
}