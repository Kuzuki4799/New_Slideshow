package com.acatapps.videomaker.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.acatapps.videomaker.application.VideoMakerApplication

class InternetStateChange : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        VideoMakerApplication.instance.loadAd()
        BusUtils.notifyChangeNetworkStateOff()
    }
}