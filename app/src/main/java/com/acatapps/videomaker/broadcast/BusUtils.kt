package com.acatapps.videomaker.broadcast

import org.greenrobot.eventbus.EventBus

object BusUtils {
    fun notifyChangeNetworkStateOff() {
        EventBus.getDefault().post(NetworkChangeEvent())
    }
}