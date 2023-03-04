package com.hope_studio.base_ads.utils

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.hope_studio.base_ads.BuildConfig

object AnalyticsUtils {

    private val firebaseAnalytics = MutableLiveData<FirebaseAnalytics>()

    fun initFirebaseAnalytic(context: Context) {
        firebaseAnalytics.value = FirebaseAnalytics.getInstance(context)
    }

    fun pushEventAnalytic(nameEvent: String, bundle: Bundle?) {
        if (!BuildConfig.DEBUG) firebaseAnalytics.value?.logEvent(nameEvent, bundle)
    }
}