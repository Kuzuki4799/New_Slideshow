package com.hope_studio.base_ads.model

class DataModel : BaseResponse() {
    var version = ""
    var data = ArrayList<Data>()
    var show_ads = false
    var show_banner = false
    var show_native_banner = false
    var show_native = false
    var show_native_scroll = false
    var show_interstitial = false
    var show_interstitial_scroll = false
    var offset_native = 0
    var offset_show_interstitial = 0
    var banner_or_native = false
    var show_reward = false
    var show_open_app = false
    var key_open_app = ""
    var show_first_open = false
    var show_exit_app = false
    var splash_open = false
    var preload = false
    var play_billing_ads = false
    var privacy_policy = ""
    var email = ""
    var link_update = ""
    var link_more_app = ""
    var more_apps = ArrayList<MoreApp>()

    class Data {
        var type = ""
        var app_id = ""
        var banner = ""
        var banner_native = ""
        var interstitial = ""
        var native = ""
        var reward = ""
    }

    class MoreApp {
        var name = ""
        var image = ""
        var banner = ""
        var url = ""
    }

    fun getSizeAds(): Int {
        return data.size
    }

    fun getAdsByType(type: String): Data {
        var dataType = Data()
        for (i in data) if (i.type == type) dataType = i
        return dataType
    }

    fun getTimeShow(): Int {
        return offset_show_interstitial
    }

    fun getCountNative(): Int {
        return offset_native
    }

    fun getShowAds(): Boolean {
        return show_ads
    }

    fun getShowBannerAds(): Boolean {
        return show_banner
    }

    fun getShowBannerNativeAds(): Boolean {
        return show_native_banner
    }

    fun getShowNativeAds(): Boolean {
        return show_native
    }

    fun getShowNativeScroll(): Boolean {
        return show_native_scroll
    }

    fun getShowInterstitialAds(): Boolean {
        return show_interstitial
    }

    fun getShowInterstitialScroll(): Boolean {
        return show_interstitial_scroll
    }

    fun getShowRewardAds(): Boolean {
        return show_reward
    }

    fun getShowOpenAds(): Boolean {
        return show_open_app
    }

    fun getPlayBilling(): Boolean {
        return play_billing_ads
    }

    fun getMoreApps(): ArrayList<MoreApp> {
        return more_apps
    }

    fun getLinkMoreApps(): String {
        return link_more_app
    }

    fun getPrivacyPolicy(): String {
        return privacy_policy
    }

    fun getEmailApp(): String {
        return email
    }

    fun getVersionApp(): String {
        return version
    }

    fun getLinkUpdate(): String {
        return link_update
    }

    fun getShowFirstOpen(): Boolean {
        return show_first_open
    }

    fun getShowExitApp(): Boolean {
        return show_exit_app
    }

    fun getSplashOpen(): Boolean {
        return splash_open
    }

    fun getBannerOrNative(): Boolean {
        return banner_or_native
    }

    fun getShowPreload(): Boolean {
        return preload
    }
}