package com.hope_studio.base_ads.utils

import org.greenrobot.eventbus.EventBus

object EventBusUtil {
    fun register(o: Any?) {
        if (!EventBus.getDefault().isRegistered(o)) {
            EventBus.getDefault().register(o)
        }
    }

    fun unregister(o: Any?) {
        if (EventBus.getDefault().isRegistered(o)) {
            EventBus.getDefault().unregister(o)
        }
    }

    private fun hasSubscriber(c: Class<*>?): Boolean {
        return EventBus.getDefault().hasSubscriberForEvent(c)
    }

    fun post(o: Any) {
        try {
            if (hasSubscriber(o.javaClass)) {
                EventBus.getDefault().post(o)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(o)
        }
    }
}