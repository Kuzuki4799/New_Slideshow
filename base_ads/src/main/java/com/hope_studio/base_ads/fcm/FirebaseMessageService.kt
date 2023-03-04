package com.hope_studio.base_ads.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hope_studio.base_ads.BuildConfig

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        if (BuildConfig.DEBUG) {
            Log.d("base_ads", "Token: $p0")
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        if (BuildConfig.DEBUG) {
            Log.d("base_ads", "Notification: ${p0.notification?.body}")
        }
    }
}