package com.hope_studio.base_ads.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.gms.ads.nativead.NativeAd
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.ads.widget.NativeAds
import com.hope_studio.base_ads.ads.widget.NativeBannerAds
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.base.BaseDialog
import com.hope_studio.base_ads.dialog.DialogWatchAds
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.utils.*
import com.androidnetworking.error.ANError

import com.androidnetworking.interfaces.ParsedRequestListener

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.facebook.ads.NativeBannerAd
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.dialog.LoadingDialog
import com.hope_studio.base_ads.widget.AdsView


object BaseAds {
    var nameInterstitialPos = "ADMOB"
    var nameRewardPos = "ADMOB"

    var isFinishApp = false
    var TIME_CONFIG_SHOW = "TIME_CONFIG_SHOW"
    private var onRewardDoneCallback: OnRewardDoneCallback? = null

    var isShowAds = false

    fun resetData(activity: Activity) {
        isShowAds = false
        isFinishApp = false
        onRewardDoneCallback = null
        TimerApp.resetTime(activity)
        GoogleAds.resetData()
        FacebookAds.resetData()
        ApplovinAd.resetData()
    }

    fun initAds(activity: Activity, dataModel: DataModel) {
//        GoogleAds.initAppId(activity, dataModel.getAdsByType(AdsType.ADMOB.value).app_id)
//        ApplovinAd.initAppId(activity)
    }

    interface OnCallApiCallback {
        fun onCallSuccess(result: DataModel)

        fun onCallFail()
    }

    fun callApiAds(
        context: Context, codeApp: Int, url: String, onCallApiCallback: OnCallApiCallback
    ) {
        AndroidNetworking.get(url + ApiUtils.APPLICATION_ID)
            .addQueryParameter("id", "$codeApp")
            .setPriority(Priority.HIGH)
            .setOkHttpClient(ApiUtils.getOkHttpClient())
            .build()
            .getAsObject(DataModel::class.java, object : ParsedRequestListener<DataModel> {
                override fun onResponse(result: DataModel) {
                    ShareUtils.put(context, DataModel::class.java.name, result)
                    onCallApiCallback.onCallSuccess(result)
                }

                override fun onError(e: ANError) {
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Load Api Error: ${e.localizedMessage}")
                    }
                    onCallApiCallback.onCallFail()
                }
            })
    }

    private fun callbackBanner(
        activity: BaseActivity, view: LinearLayout, position: Int, listener: (() -> Unit)? = null
    ): OnBannerAdsCallback {
        return object : OnBannerAdsCallback {
            override fun onBannerLoadSuccess() {
                listener?.invoke()
            }

            override fun onBannerLoadFail() {
                loadBanner(activity, position + 1, view)
            }
        }
    }

    fun loadBanner(
        activity: BaseActivity, position: Int, view: LinearLayout, listener: (() -> Unit)? = null
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            listener?.invoke()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            listener?.invoke()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            listener?.invoke()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            listener?.invoke()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowBannerAds()) {
            listener?.invoke()
            return
        }
        if (position == dataAds.getSizeAds()) {
            listener?.invoke()
            return
        }
        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.loadBannerGoogleAds(
                    activity, dataPos.banner, view,
                    callbackBanner(activity, view, position, listener)
                )
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.loadBannerFacebookAds(
                    activity, dataPos.banner, view,
                    callbackBanner(activity, view, position, listener)
                )
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.loadBannerApplovinAds(
                    activity, dataPos.banner, view,
                    callbackBanner(activity, view, position, listener)
                )
            }
        }
    }

    fun loadBaseNativeOrBanner(
        activity: BaseActivity,
        position: Int, linearLayout: LinearLayout, nativeBannerAd: NativeBannerAds, width: Int
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            nativeBannerAd.removeAllViews()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            nativeBannerAd.removeAllViews()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            nativeBannerAd.removeAllViews()
            return
        }

        if (BillingUtils.getDataBilling(activity)) {
            nativeBannerAd.removeAllViews()
            return
        }
        if (!dataAds.getShowAds()) {
            nativeBannerAd.removeAllViews()
            return
        }
//        if (!dataAds.getShowAds() || !dataAds.getShowBannerNativeAds()) {
//            nativeBannerAd.removeAllViews()
//            return
//        }
        if (position == dataAds.getSizeAds()) {
            nativeBannerAd.removeAllViews()
            return
        }

//        val dataPos = dataAds.data[position]
        nativeBannerAd.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        nativeBannerAd.visibility = View.VISIBLE

//        if (dataAds.getBannerOrNative()) {
        loadBanner(activity, 0, linearLayout) {
            nativeBannerAd.visibility = View.GONE
        }
//            return
//        }

//        when (dataPos.type) {
//            AdsType.ADMOB.value -> {
//                GoogleAds.initBaseNativeBannerAds(
//                    activity, dataPos.banner_native, object : OnNativeAdCallback<NativeAd> {
//                        override fun onNativeLoadSuccess(nativeAd: NativeAd) {
//                            try {
//                                val viewAdd =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
//                                val viewShimmerLayout =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)
//                                nativeBannerAd.setGoogleNativeAd(nativeAd)
//                                viewAdd.visibility = View.GONE
//                                viewShimmerLayout.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                                e.localizedMessage
//                                nativeBannerAd.removeAllViews()
//                            }
//                        }
//
//                        override fun onNativeLoadFail() {
//                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
//                        }
//                    })
//            }
//
//            AdsType.FACEBOOK.value -> {
//                FacebookAds.initBaseNativeBannerFacebookAds(
//                    activity, dataPos.banner_native,
//                    object : OnNativeAdCallback<NativeBannerAd> {
//                        override fun onNativeLoadSuccess(nativeAd: NativeBannerAd) {
//                            try {
//                                val viewAdd =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
//                                val viewShimmerLayout =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)
//                                nativeBannerAd.setFacebookNativeAd(nativeAd)
//                                viewAdd.visibility = View.GONE
//                                viewShimmerLayout.visibility = View.VISIBLE
//                            } catch (e: Exception) {
//                                e.localizedMessage
//                                nativeBannerAd.removeAllViews()
//                            }
//                        }
//
//                        override fun onNativeLoadFail() {
//                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
//                        }
//                    })
//            }
//
//            AdsType.APPLOVIN.value -> {
//                ApplovinAd.initBaseNativeBannerAds(
//                    activity, dataPos.banner_native, width,
//                    object : OnNativeAdCallback<MaxAdView> {
//                        override fun onNativeLoadSuccess(nativeAd: MaxAdView) {
//                            try {
//                                val viewAdd =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
//                                val viewShimmerLayout =
//                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)
//
//                                viewAdd.setPadding(4, 4, 4, 4)
//                                viewAdd.removeAllViews()
//                                viewAdd.addView(nativeAd)
//                                viewAdd.addView(AdsView(activity))
//                                viewAdd.setBackgroundResource(R.drawable.bg_border_ads)
//
//                                viewAdd.visibility = View.VISIBLE
//                                viewShimmerLayout.visibility = View.GONE
//                            } catch (e: Exception) {
//                                e.localizedMessage
//                                nativeBannerAd.removeAllViews()
//                            }
//                        }
//
//                        override fun onNativeLoadFail() {
//                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
//                        }
//                    })
//            }
//
//            else -> {
//                loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
//            }
//        }
    }

    fun getKeyNativeApplovin(context: Context): String {
        if (!NetWorkUtils.isNetworkConnected(context)) return ""
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return ""
        if (dataAds.getSizeAds() == 0) return ""
        if (BillingUtils.getDataBilling(context)) return ""
        if (!dataAds.getShowAds() || !dataAds.getShowNativeAds() || !dataAds.getShowNativeScroll()) return ""
        return dataAds.getAdsByType(AdsType.APPLOVIN.value).native
    }

    fun getKeyNativeBannerApplovin(context: Context): String {
        if (!NetWorkUtils.isNetworkConnected(context)) return ""
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return ""
        if (dataAds.getSizeAds() == 0) return ""
        if (BillingUtils.getDataBilling(context)) return ""
        if (!dataAds.getShowAds() || !dataAds.getShowBannerNativeAds() || !dataAds.getShowNativeScroll()) return ""
        return dataAds.getAdsByType(AdsType.APPLOVIN.value).banner_native
    }

    fun loadBaseNativeBannerAd(
        activity: Context, position: Int, nativeBannerAd: NativeBannerAds, width: Int
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            nativeBannerAd.removeAllViews()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            nativeBannerAd.removeAllViews()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            nativeBannerAd.removeAllViews()
            return
        }

        if (BillingUtils.getDataBilling(activity)) {
            nativeBannerAd.removeAllViews()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowBannerNativeAds()) {
            nativeBannerAd.removeAllViews()
            return
        }
        if (position == dataAds.getSizeAds()) {
            nativeBannerAd.removeAllViews()
            return
        }

        val dataPos = dataAds.data[position]
        nativeBannerAd.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        nativeBannerAd.visibility = View.VISIBLE

        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.initBaseNativeBannerAds(
                    activity, dataPos.banner_native, object : OnNativeAdCallback<NativeAd> {
                        override fun onNativeLoadSuccess(nativeAd: NativeAd) {
                            try {
                                val viewAdd =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)
                                nativeBannerAd.setGoogleNativeAd(nativeAd)
                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeBannerAd.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
                        }
                    })
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.initBaseNativeBannerFacebookAds(
                    activity, dataPos.banner_native,
                    object : OnNativeAdCallback<NativeBannerAd> {
                        override fun onNativeLoadSuccess(nativeAd: NativeBannerAd) {
                            try {
                                val viewAdd =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)
                                nativeBannerAd.setFacebookNativeAd(nativeAd)
                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeBannerAd.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
                        }
                    })
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.initBaseNativeBannerAds(
                    activity, dataPos.banner_native, width,
                    object : OnNativeAdCallback<MaxAdView> {
                        override fun onNativeLoadSuccess(nativeAd: MaxAdView) {
                            try {
                                val viewAdd =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeBannerAd.findViewById<FrameLayout>(R.id.shimmerLayout)

                                viewAdd.setPadding(4, 4, 4, 4)
                                viewAdd.removeAllViews()
                                viewAdd.addView(nativeAd)
                                viewAdd.addView(AdsView(activity))
                                viewAdd.setBackgroundResource(R.drawable.bg_border_ads)

                                viewAdd.visibility = View.VISIBLE
                                viewShimmerLayout.visibility = View.GONE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeBannerAd.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
                        }
                    })
            }

            else -> {
                loadBaseNativeBannerAd(activity, position + 1, nativeBannerAd, width)
            }
        }
    }

    fun getPreLoad(activity: Activity): Boolean {
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return false
        return dataAds.getShowPreload()
    }

    fun loadBaseNativeAd(activity: Context, position: Int, nativeAds: NativeAds, width: Int) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            nativeAds.removeAllViews()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            nativeAds.removeAllViews()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            nativeAds.removeAllViews()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            nativeAds.removeAllViews()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowNativeAds()) {
            nativeAds.visibility = View.VISIBLE
            nativeAds.removeAllViews()
            return
        }
        if (position == dataAds.getSizeAds()) {
            nativeAds.removeAllViews()
            return
        }
        nativeAds.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        nativeAds.visibility = View.VISIBLE
        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.initBaseNativeAds(
                    activity, dataPos.native, object : OnNativeAdCallback<NativeAd> {
                        override fun onNativeLoadSuccess(nativeAd: NativeAd) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)
                                nativeAds.setGoogleNativeAd(nativeAd)

                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAd(activity, position + 1, nativeAds, width)
                        }
                    })
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.initBaseNativeFacebookAds(
                    activity, dataPos.native,
                    object : OnNativeAdCallback<com.facebook.ads.NativeAd> {
                        override fun onNativeLoadSuccess(nativeAd: com.facebook.ads.NativeAd) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)
                                nativeAds.setFacebookNativeAd(nativeAd)

                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAd(activity, position + 1, nativeAds, width)
                        }
                    })
            }

            AdsType.APPLOVIN.value -> {
                val unitId = dataPos.native
                ApplovinAd.initBaseNativeAds(
                    activity, unitId, width, dataAds,
                    object : OnNativeAdCallback2<MaxAdView?, MaxNativeAdView?> {
                        override fun onNativeLoadSuccess(
                            nativeAd: MaxAdView?,
                            native2: MaxNativeAdView?
                        ) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)

                                viewAdd.setPadding(4, 4, 4, 4)
                                viewAdd.removeAllViews()
                                if (nativeAd == null) {
                                    native2?.layoutParams = LinearLayout.LayoutParams(
                                        width, ((width / 5) * 4.0).toInt()
                                    )
                                    viewAdd.addView(native2)
                                } else {
                                    viewAdd.addView(nativeAd)
                                }
                                viewAdd.addView(AdsView(activity))
                                viewAdd.setBackgroundResource(R.drawable.bg_border_ads)

                                viewAdd.visibility = View.VISIBLE
                                viewShimmerLayout.visibility = View.GONE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAd(activity, position + 1, nativeAds, width)
                        }
                    })
            }

            else -> {
                loadBaseNativeAd(activity, position + 1, nativeAds, width)
            }
        }
    }

    fun loadBaseNativeAdCallback(
        activity: Context, position: Int, nativeAds: NativeAds,
        onNativeLoadCallback: OnNativeCallback, width: Int
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowNativeAds()) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }
        if (position == dataAds.getSizeAds()) {
            nativeAds.removeAllViews()
            onNativeLoadCallback.onNativeFail()
            return
        }

        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.initBaseNativeAds(
                    activity, dataPos.native, object : OnNativeAdCallback<NativeAd> {
                        override fun onNativeLoadSuccess(nativeAd: NativeAd) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)

                                nativeAds.visibility = View.VISIBLE
                                nativeAds.setGoogleNativeAd(nativeAd)
                                onNativeLoadCallback.onNativeSuccess()

                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAdCallback(
                                activity, position + 1, nativeAds, onNativeLoadCallback, width
                            )
                        }
                    })
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.initBaseNativeFacebookAds(
                    activity, dataPos.native,
                    object : OnNativeAdCallback<com.facebook.ads.NativeAd> {
                        override fun onNativeLoadSuccess(nativeAd: com.facebook.ads.NativeAd) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)

                                nativeAds.visibility = View.VISIBLE
                                nativeAds.setFacebookNativeAd(nativeAd)
                                onNativeLoadCallback.onNativeSuccess()

                                viewAdd.visibility = View.GONE
                                viewShimmerLayout.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAdCallback(
                                activity, position + 1, nativeAds, onNativeLoadCallback, width
                            )
                        }
                    })
            }

            AdsType.APPLOVIN.value -> {
                val unitId = dataPos.native
                ApplovinAd.initBaseNativeAds(
                    activity, unitId, width, dataAds,
                    object : OnNativeAdCallback2<MaxAdView?, MaxNativeAdView?> {
                        override fun onNativeLoadSuccess(
                            nativeAd: MaxAdView?,
                            native2: MaxNativeAdView?
                        ) {
                            try {
                                val viewAdd = nativeAds.findViewById<FrameLayout>(R.id.layoutAddAds)
                                val viewShimmerLayout =
                                    nativeAds.findViewById<FrameLayout>(R.id.shimmerLayout)

                                viewAdd.setPadding(4, 4, 4, 4)
                                nativeAds.visibility = View.VISIBLE
                                viewAdd.removeAllViews()

                                if (nativeAd == null) {
                                    native2?.layoutParams = LinearLayout.LayoutParams(
                                        width, ((width / 5) * 4.0).toInt()
                                    )
                                    viewAdd.addView(native2)
                                } else {
                                    viewAdd.addView(nativeAd)
                                }

                                viewAdd.addView(AdsView(activity))
                                viewAdd.setBackgroundResource(R.drawable.bg_border_ads)
                                onNativeLoadCallback.onNativeSuccess()

                                viewAdd.visibility = View.VISIBLE
                                viewShimmerLayout.visibility = View.GONE
                            } catch (e: Exception) {
                                e.localizedMessage
                                nativeAds.removeAllViews()
                            }
                        }

                        override fun onNativeLoadFail() {
                            loadBaseNativeAdCallback(
                                activity, position + 1, nativeAds, onNativeLoadCallback, width
                            )
                        }
                    })
            }

            else -> {
                loadBaseNativeAdCallback(
                    activity, position + 1, nativeAds, onNativeLoadCallback, width
                )
            }
        }
    }

    fun showInterstitialAdExit(activity: BaseActivity) {
        isFinishApp = true
        val dialogLoad = com.hope_studio.base_ads.dialog.LoadingDialog(activity)
        loadAndShowInterstitialAd(activity, dialogLoad, 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.finishAndRemoveTask()
            } else {
                activity.finish()
            }
        }
    }

    fun showInterstitialAd(activity: BaseActivity, position: Int) {
        if (!NetWorkUtils.isNetworkConnected(activity)) return
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataModel == null) return
        if (BillingUtils.getDataBilling(activity)) return
        if (!dataModel.getShowAds() || !dataModel.getShowInterstitialAds()) return
        if (position == dataModel.getSizeAds()) return

        when (dataModel.data[position].type) {
            AdsType.ADMOB.value -> {
                if (GoogleAds.getInterstitialGoogleAd() != null) {
                    GoogleAds.showInterstitialGoogleAds(activity)
                } else showInterstitialAd(activity, position + 1)
            }

            AdsType.FACEBOOK.value -> {
                if (FacebookAds.getInterstitialFacebookAd() != null) {
                    if (FacebookAds.getInterstitialFacebookAd()!!.isAdLoaded) {
                        FacebookAds.showInterstitialFacebookAds()
                    } else showInterstitialAd(activity, position + 1)
                } else showInterstitialAd(activity, position + 1)
            }

            AdsType.APPLOVIN.value -> {
                if (ApplovinAd.getInterstitial() != null) {
                    if (ApplovinAd.getInterstitial()?.isReady == true) {
                        ApplovinAd.getInterstitial()?.showAd()
                    } else showInterstitialAd(activity, position + 1)
                } else showInterstitialAd(activity, position + 1)
            }

            else -> showInterstitialAd(activity, position + 1)
        }
    }

    fun preloadInterstitial(activity: Activity): Boolean {
        if (!NetWorkUtils.isNetworkConnected(activity)) return false
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataModel == null) return false
        return dataModel.getShowPreload()
    }

    private fun callbackShowInterstitial(
        activity: Activity, position: Int, type: String,
        dialog: com.hope_studio.base_ads.dialog.LoadingDialog, listener: () -> Unit
    ): OnInterstitialAdCallback {
        return object : OnInterstitialAdCallback {
            override fun onInterstitialClose() {
                isShowAds = false
                if (isFinishApp) {
                    AnalyticsUtils.pushEventAnalytic("exit_app", null)
                    (activity as BaseActivity).openExitApp()
                } else {
                    listener.invoke()
                    ShareUtils.putLong(activity, TIME_CONFIG_SHOW, System.currentTimeMillis())
                    if (preloadInterstitial(activity)) {
                        loadInterstitialAd(activity, 0)
                    }
                }
            }

            override fun onInterstitialShowFail() {
                isShowAds = false
                if (isFinishApp) {
                    AnalyticsUtils.pushEventAnalytic("exit_app", null)
                    (activity as BaseActivity).openExitApp()
                } else {
                    listener.invoke()
                    if (preloadInterstitial(activity)) {
                        loadInterstitialAd(activity, 0)
                    }
                }
            }

            override fun onInterstitialLoadSuccess() {
                if (preloadInterstitial(activity)) {
                    try {
                        Handler().postDelayed({
                            dialog.dismissDialog()
                        }, 100)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    nameInterstitialPos = type
                    showInterstitialAd((activity as BaseActivity), position)
                } else {
                    try {
                        Handler().postDelayed({
                            dialog.dismissDialog()
                        }, 100)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    nameInterstitialPos = type
                    showInterstitialAd((activity as BaseActivity), position)
                }
            }

            override fun onInterstitialLoadFail() {
                loadAndShowInterstitialAd(activity, dialog, position + 1, listener)
            }
        }
    }

    private fun callbackShowGifInterstitial(
        activity: Activity, dialogLoad: com.hope_studio.base_ads.dialog.LoadingDialog, position: Int,
        type: String, dialog: com.hope_studio.base_ads.dialog.LoadingDialog, listener: () -> Unit
    ): OnInterstitialAdCallback {
        return object : OnInterstitialAdCallback {
            override fun onInterstitialClose() {
                isShowAds = false
                Handler().postDelayed({
                    try {
                        dialog.dismissDialog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 100)
                if (preloadInterstitial(activity)) {
                    loadInterstitialAd(activity, 0)
                }
            }

            override fun onInterstitialShowFail() {
                isShowAds = false
                if (preloadInterstitial(activity)) {
                    loadInterstitialAd(activity, 0)
                }
            }

            override fun onInterstitialLoadSuccess() {
                nameInterstitialPos = type
                showInterstitialAd((activity as BaseActivity), position)
            }

            override fun onInterstitialLoadFail() {
                loadAndShowGifInterstitialAd(activity, dialogLoad, position + 1, listener)
            }
        }
    }

    private fun callbackShowInterstitialNoPreload(
        activity: Activity, position: Int, type: String
    ): OnInterstitialAdCallback {
        return object : OnInterstitialAdCallback {
            override fun onInterstitialClose() {
                isShowAds = false
                if (isFinishApp) {
                    AnalyticsUtils.pushEventAnalytic("exit_app", null)
                    (activity as BaseActivity).openExitApp()
                } else {
                    listener?.invoke()
                    ShareUtils.putLong(activity, TIME_CONFIG_SHOW, System.currentTimeMillis())
                    loadInterstitialAd(activity, 0)
                }
            }

            override fun onInterstitialShowFail() {
                isShowAds = false
                if (isFinishApp) {
                    AnalyticsUtils.pushEventAnalytic("exit_app", null)
                    (activity as BaseActivity).openExitApp()
                } else {
                    listener?.invoke()
                }
                loadInterstitialAd(activity, 0)
            }

            override fun onInterstitialLoadSuccess() {
                nameInterstitialPos = type
            }

            override fun onInterstitialLoadFail() {
                loadInterstitialAd(activity, position + 1)
            }
        }
    }

    fun getInterstitialReady(activity: Activity): Boolean {
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataModel == null) return false
        if (!dataModel.getShowAds() || !dataModel.getShowInterstitialAds()) return false

        for (i in dataModel.data) {
            when (i.type) {
                AdsType.ADMOB.value -> {
                    if (GoogleAds.getInterstitialGoogleAd() != null) {
                        return true
                    }
                }

                AdsType.FACEBOOK.value -> {
                    if (FacebookAds.getInterstitialFacebookAd() != null) {
                        if (FacebookAds.getInterstitialFacebookAd()!!.isAdLoaded) {
                            return true
                        }
                    }
                }

                AdsType.APPLOVIN.value -> {
                    if (ApplovinAd.getInterstitial() != null) {
                        if (ApplovinAd.getInterstitial()!!.isReady) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun loadAndShowGifInterstitialAd(
        activity: Activity, dialogLoad: com.hope_studio.base_ads.dialog.LoadingDialog, position: Int, listener: () -> Unit
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            return
        }
        if (dataAds.getSizeAds() == 0) {
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowInterstitialAds()) {
            return
        }
        if (position == dataAds.getSizeAds()) {
            return
        }

        if (getInterstitialReady(activity)) {
            this@BaseAds.listener = listener
            showInterstitialAd(activity as BaseActivity, 0)
            return
        }

        if (!preloadInterstitial(activity)) {
            try {
                if (!dialogLoad.dialogIsShowing()) {
                    dialogLoad.showDialog(activity, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialogLoad.showDialog(activity, false)
            }
        }

        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.loadInterstitialGoogleAds(
                    activity, dataPos.interstitial,
                    callbackShowGifInterstitial(
                        activity, dialogLoad, position, AdsType.ADMOB.value, dialogLoad, listener
                    )
                )
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.loadInterstitialFacebookAds(
                    activity, dataPos.interstitial,
                    callbackShowGifInterstitial(
                        activity, dialogLoad, position, AdsType.FACEBOOK.value, dialogLoad, listener
                    )
                )
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.loadInterstitialApplovinAd(
                    activity, dataPos.interstitial,
                    callbackShowGifInterstitial(
                        activity, dialogLoad, position, AdsType.APPLOVIN.value, dialogLoad, listener
                    )
                )
            }

            else -> loadAndShowGifInterstitialAd(activity, dialogLoad, position + 1, listener)
        }
    }

    fun loadInterstitialAd(activity: Activity, position: Int) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            return
        }
        if (dataAds.getSizeAds() == 0) {
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowInterstitialAds()) {
            return
        }
        if (position == dataAds.getSizeAds()) {
            return
        }

        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.loadInterstitialGoogleAds(
                    activity, dataPos.interstitial,
                    callbackShowInterstitialNoPreload(activity, position, AdsType.ADMOB.value)
                )
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.loadInterstitialFacebookAds(
                    activity, dataPos.interstitial,
                    callbackShowInterstitialNoPreload(activity, position, AdsType.FACEBOOK.value)
                )
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.loadInterstitialApplovinAd(
                    activity, dataPos.interstitial,
                    callbackShowInterstitialNoPreload(activity, position, AdsType.APPLOVIN.value)
                )
            }

            else -> loadInterstitialAd(activity, position + 1)
        }
    }

    private var listener: (() -> Unit)? = null

    fun loadAndShowInterstitialAd(
        activity: Activity, dialogLoad: com.hope_studio.base_ads.dialog.LoadingDialog, position: Int, listener: () -> Unit
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            listener.invoke()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            listener.invoke()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            listener.invoke()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            listener.invoke()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowInterstitialAds()) {
            listener.invoke()
            return
        }
        if (position == dataAds.getSizeAds()) {
            listener.invoke()
            return
        }

        if (!TimerApp.checkTimeShowConfig(activity, dataAds.getTimeShow())) {
            if (!isFinishApp) {
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Not Enough Time Show")
                }
                listener.invoke()
                return
            } else {
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Exit when Not Enough Time Show")
                }
            }
        }

        if (getInterstitialReady(activity)) {
            this@BaseAds.listener = listener
            showInterstitialAd(activity as BaseActivity, 0)
            return
        }

        if (!preloadInterstitial(activity)) {
            try {
                if (!dialogLoad.dialogIsShowing()) {
                    dialogLoad.showDialog(activity, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialogLoad.showDialog(activity, false)
            }
        }

        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.loadInterstitialGoogleAds(
                    activity, dataPos.interstitial,
                    callbackShowInterstitial(
                        activity, position, AdsType.ADMOB.value, dialogLoad, listener
                    )
                )
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.loadInterstitialFacebookAds(
                    activity, dataPos.interstitial,
                    callbackShowInterstitial(
                        activity, position, AdsType.FACEBOOK.value, dialogLoad, listener
                    )
                )
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.loadInterstitialApplovinAd(
                    activity, dataPos.interstitial,
                    callbackShowInterstitial(
                        activity, position, AdsType.APPLOVIN.value, dialogLoad, listener
                    )
                )
            }

            else -> loadAndShowInterstitialAd(activity, dialogLoad, position + 1, listener)
        }
    }

    fun showRewardAd(
        activity: Activity, onRewardDoneCallback: OnRewardDoneCallback
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        this.onRewardDoneCallback = onRewardDoneCallback
        val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataModel == null) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (dataModel.getSizeAds() == 0) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (!dataModel.getShowAds() || !dataModel.getShowRewardAds()) {
            onRewardDoneCallback.onRewardDone()
            return
        }

        when (nameRewardPos) {
            AdsType.ADMOB.value -> {
                if (GoogleAds.getRewardedGoogleAd() != null) {
                    GoogleAds.showRewardGoogleAds(activity)
                } else onRewardDoneCallback.onRewardDone()
            }

            AdsType.FACEBOOK.value -> {
                if (FacebookAds.getRewardedInterstitialFacebookAd() != null) {
                    if (FacebookAds.getRewardedInterstitialFacebookAd()!!.isAdLoaded) {
                        FacebookAds.getRewardedInterstitialFacebookAd()?.show()
                    } else onRewardDoneCallback.onRewardDone()
                } else onRewardDoneCallback.onRewardDone()
            }

            AdsType.APPLOVIN.value -> {
                if (ApplovinAd.getReward() != null) {
                    if (ApplovinAd.getReward()?.isReady == true) {
                        ApplovinAd.getReward()?.showAd()
                    } else onRewardDoneCallback.onRewardDone()
                } else onRewardDoneCallback.onRewardDone()
            }
        }
    }

    private fun callbackShowReward(
        activity: Activity, position: Int, type: String, dialog: com.hope_studio.base_ads.dialog.LoadingDialog,
        onRewardDoneCallback: OnRewardDoneCallback
    ): OnRewardAdCallback {
        return object : OnRewardAdCallback {
            override fun onRewardClose() {
                Handler().postDelayed({
                    dialog.dismissDialog()
                }, 300)
                isShowAds = false
                onRewardDoneCallback.onRewardDone()
                ShareUtils.putLong(activity, TIME_CONFIG_SHOW, System.currentTimeMillis())
            }

            override fun onRewardShowFail() {
                isShowAds = false
                dialog.dismissDialog()
                onRewardDoneCallback.onRewardDone()
            }

            override fun onRewardLoadSuccess() {
                nameRewardPos = type
                showRewardAd(activity, onRewardDoneCallback)
            }

            override fun onRewardLoadFail() {
                loadAndShowRewardAd(activity, dialog, position + 1, onRewardDoneCallback)
            }
        }
    }

    fun dialogLoadAndShowReward(
        activity: BaseActivity,
        hasDialog: Boolean, str: String, onRewardDoneCallback: OnRewardDoneCallback
    ) {
        if (!NetWorkUtils.isNetworkConnected(activity)) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (dataAds.getSizeAds() == 0) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (BillingUtils.getDataBilling(activity)) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (!dataAds.getShowAds() || !dataAds.getShowRewardAds()) {
            onRewardDoneCallback.onRewardDone()
            return
        }

        val dialogLoad = com.hope_studio.base_ads.dialog.LoadingDialog(activity)
        if (hasDialog) {
            DialogWatchAds(activity, str, object : BaseDialog.OnDialogCallback {
                override fun onDialogListener() {
                    loadAndShowRewardAd(activity, dialogLoad, 0, onRewardDoneCallback)
                }
            }).show()
        } else {
            loadAndShowRewardAd(activity, dialogLoad, 0, onRewardDoneCallback)
        }
    }

    fun loadAndShowRewardAd(
        activity: Activity, dialogLoad: com.hope_studio.base_ads.dialog.LoadingDialog, position: Int,
        onRewardDoneCallback: OnRewardDoneCallback
    ) {
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) {
            onRewardDoneCallback.onRewardDone()
            return
        }
        if (position == dataAds.getSizeAds()) {
            onRewardDoneCallback.onRewardDone()
            return
        }

        try {
            if (!dialogLoad.dialogIsShowing()) {
                dialogLoad.showDialog(activity, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dialogLoad.showDialog(activity, false)
        }

        val dataPos = dataAds.data[position]
        when (dataPos.type) {
            AdsType.ADMOB.value -> {
                GoogleAds.loadRewardedGoogleAds(
                    activity, dataPos.reward,
                    callbackShowReward(
                        activity, position, AdsType.ADMOB.value, dialogLoad, onRewardDoneCallback
                    )
                )
            }

            AdsType.FACEBOOK.value -> {
                FacebookAds.loadRewardedInterstitialFacebookAds(
                    activity, dataPos.reward,
                    callbackShowReward(
                        activity, position, AdsType.FACEBOOK.value, dialogLoad, onRewardDoneCallback
                    )
                )
            }

            AdsType.APPLOVIN.value -> {
                ApplovinAd.loadRewardApplovinAds(
                    activity, dataPos.reward,
                    callbackShowReward(
                        activity, position, AdsType.APPLOVIN.value,
                        dialogLoad, onRewardDoneCallback
                    )
                )
            }
        }
    }

    interface OnBannerAdsCallback {
        fun onBannerLoadSuccess()

        fun onBannerLoadFail()
    }

    interface OnNativeAdCallback<N> {
        fun onNativeLoadSuccess(nativeAd: N)

        fun onNativeLoadFail()
    }

    interface OnNativeAdCallback2<N, M> {
        fun onNativeLoadSuccess(nativeAd: N, native2: M)

        fun onNativeLoadFail()
    }

    interface OnInterstitialAdCallback {

        fun onInterstitialClose()

        fun onInterstitialShowFail()

        fun onInterstitialLoadSuccess()

        fun onInterstitialLoadFail()
    }

    interface OnRewardAdCallback {

        fun onRewardClose()

        fun onRewardShowFail()

        fun onRewardLoadSuccess()

        fun onRewardLoadFail()
    }

    interface OnRewardDoneCallback {
        fun onRewardDone()
    }

    interface OnNativeCallback {
        fun onNativeSuccess()

        fun onNativeFail()
    }
}

