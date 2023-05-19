package com.hope_studio.base_ads.ads.widget

import android.content.Context
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.makeramen.roundedimageview.RoundedImageView
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.widget.ShimmerLayout
import java.util.ArrayList

class NativeBannerAds : FrameLayout {

    private var nativeType = NativeType.DEFAULT.value

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs)
    }

    fun setNativeType(nativeType: NativeType) {
        this.nativeType = nativeType.value
        invalidate()
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_native_banner_ads, this)
        findViewById<com.hope_studio.base_ads.widget.ShimmerLayout>(R.id.shimmerLayout).startShimmerAnimation()

        val param = context.theme.obtainStyledAttributes(
            attrs, R.styleable.NativeBannerAds, 0, 0
        )
        nativeType = param.getInt(R.styleable.NativeBannerAds_dark_mode, 0)
        visibility = View.GONE
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }

    fun setGoogleNativeAd(nativeAd: NativeAd) {
        val nativeAdView = findViewById<NativeAdView>(R.id.native_ad_view)
        val nativeAdLayout = findViewById<NativeAdLayout>(R.id.native_ad_view_facebook)

        val iconView = findViewById<RoundedImageView>(R.id.icon)
        val nativeAdIcon = findViewById<MediaView>(R.id.icon_facebook)

        val primaryView = findViewById<TextView>(R.id.primary)
        val secondaryView = findViewById<TextView>(R.id.secondary)

        val adNotificationView = findViewById<TextView>(R.id.ad_notification_view)
        val adFacebook = findViewById<LinearLayout>(R.id.ad_notification_view_facebook)

        val callToActionView = findViewById<AppCompatTextView>(R.id.cta)
        val rlIcon = findViewById<RelativeLayout>(R.id.rlIcon)

        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val icon = nativeAd.icon

        adFacebook.visibility = View.INVISIBLE

        nativeAdIcon.visibility = View.GONE

        nativeAdView.callToActionView = iconView
        nativeAdView.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView

        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = secondaryView
        }

        primaryView.text = headline
        callToActionView.text = cta
        secondaryView.text = body
        if (icon != null) {
            rlIcon.visibility = VISIBLE
            iconView.setImageDrawable(icon.drawable)
        } else {
            rlIcon.visibility = GONE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iconView.clipToOutline = true
        }

        adNotificationView.setTextColor(resources.getColor(R.color.gnt_black))
        adNotificationView.setBackgroundResource(R.drawable.gnt_rounded_corners_shape)
        callToActionView.setBackgroundResource(R.drawable.gnt_outline_button)
        iconView.setBackgroundResource(R.drawable.gnt_outline_transparent)

        nativeAdLayout.setBackgroundResource(R.drawable.bg_main_transparent)

        when (nativeType) {
            NativeType.DEFAULT.value -> {
                primaryView.setTextColor(resources.getColor(R.color.color_text))
                secondaryView.setTextColor(resources.getColor(R.color.color_sub))
                nativeAdView.setBackgroundResource(R.drawable.bg_blur)
            }
            NativeType.DEFAULT_FILL.value -> {
                primaryView.setTextColor(resources.getColor(R.color.color_text_fill))
                secondaryView.setTextColor(resources.getColor(R.color.color_sub_fill))
                nativeAdView.setBackgroundResource(R.drawable.bg_fill)
            }
            NativeType.LIGHT_BLUR.value -> {
                primaryView.setTextColor(resources.getColor(R.color.gnt_black))
                secondaryView.setTextColor(resources.getColor(R.color.main_black))
                nativeAdView.setBackgroundResource(R.drawable.bg_dark_blur)
            }
            NativeType.DARK_BLUR.value -> {
                primaryView.setTextColor(resources.getColor(R.color.gnt_white))
                secondaryView.setTextColor(resources.getColor(R.color.main_white))
                nativeAdView.setBackgroundResource(R.drawable.bg_light_blur)
            }
            NativeType.LIGHT.value -> {
                primaryView.setTextColor(resources.getColor(R.color.gnt_black))
                secondaryView.setTextColor(resources.getColor(R.color.main_black))
                nativeAdView.setBackgroundResource(R.drawable.bg_light)
            }
            NativeType.DARK.value -> {
                primaryView.setTextColor(resources.getColor(R.color.gnt_white))
                secondaryView.setTextColor(resources.getColor(R.color.main_white))
                nativeAdView.setBackgroundResource(R.drawable.bg_dark)
            }
        }

        primaryView.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        secondaryView.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        nativeAdView.setNativeAd(nativeAd)
        Handler().postDelayed(
            { findViewById<com.hope_studio.base_ads.widget.ShimmerLayout>(R.id.shimmerLayout).stopShimmerAnimation() },
            500
        )
    }

    fun setFacebookNativeAd(nativeAd: NativeBannerAd) {
        val nativeAdView = findViewById<NativeAdView>(R.id.native_ad_view)
        val nativeAdLayout = findViewById<NativeAdLayout>(R.id.native_ad_view_facebook)

        val iconView = findViewById<RoundedImageView>(R.id.icon)
        val nativeAdIcon = findViewById<MediaView>(R.id.icon_facebook)

        val nativeAdTitle = findViewById<TextView>(R.id.primary)
        val nativeAdBody = findViewById<TextView>(R.id.secondary)
        val nativeAdCallToAction = findViewById<AppCompatTextView>(R.id.cta)

        val adNotificationView = findViewById<TextView>(R.id.ad_notification_view)
        val adChoicesContainer = findViewById<LinearLayout>(R.id.ad_notification_view_facebook)

        if (adChoicesContainer != null) {
            val adOptionsView = AdOptionsView(context, nativeAd, nativeAdLayout)
            adChoicesContainer.removeAllViews()
            adChoicesContainer.addView(adOptionsView)
        }

        iconView.visibility = View.GONE

        adNotificationView.visibility = View.INVISIBLE

        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE

        nativeAdBody.text = nativeAd.adBodyText
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdCallToAction.text = nativeAd.adCallToAction

        nativeAdView.setBackgroundResource(R.drawable.bg_main_transparent)

        when (nativeType) {
            NativeType.DEFAULT.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.color_text))
                nativeAdBody.setTextColor(resources.getColor(R.color.color_sub))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_blur)
            }
            NativeType.DEFAULT_FILL.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.color_text_fill))
                nativeAdBody.setTextColor(resources.getColor(R.color.color_sub_fill))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_fill)
            }
            NativeType.LIGHT_BLUR.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.gnt_black))
                nativeAdBody.setTextColor(resources.getColor(R.color.main_black))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_dark_blur)
            }
            NativeType.DARK_BLUR.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.gnt_white))
                nativeAdBody.setTextColor(resources.getColor(R.color.main_white))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_light_blur)
            }
            NativeType.LIGHT.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.gnt_black))
                nativeAdBody.setTextColor(resources.getColor(R.color.main_black))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_light)
            }
            NativeType.DARK.value -> {
                nativeAdTitle.setTextColor(resources.getColor(R.color.gnt_white))
                nativeAdBody.setTextColor(resources.getColor(R.color.main_white))
                nativeAdLayout.setBackgroundResource(R.drawable.bg_dark)
            }
        }

        nativeAdCallToAction.setBackgroundResource(R.drawable.gnt_outline_button)
        nativeAdTitle.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        nativeAdBody.setBackgroundColor(context.resources.getColor(android.R.color.transparent))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nativeAdIcon.clipToOutline = true
        }
        nativeAdIcon.setBackgroundResource(R.drawable.gnt_outline_transparent)

        val clickableViews = ArrayList<View>()
        clickableViews.add(nativeAdIcon)
        clickableViews.add(nativeAdCallToAction)
        nativeAd.registerViewForInteraction(
            nativeAdLayout, nativeAdIcon, clickableViews
        )

        NativeAdBase.NativeComponentTag.tagView(
            nativeAdIcon, NativeAdBase.NativeComponentTag.AD_ICON
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdTitle, NativeAdBase.NativeComponentTag.AD_TITLE
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdBody, NativeAdBase.NativeComponentTag.AD_BODY
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdCallToAction, NativeAdBase.NativeComponentTag.AD_CALL_TO_ACTION
        )
        Handler().postDelayed(
            { findViewById<com.hope_studio.base_ads.widget.ShimmerLayout>(R.id.shimmerLayout).stopShimmerAnimation() },
            500
        )
    }
}