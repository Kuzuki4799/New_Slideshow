package com.hope_studio.base_ads.base

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class BaseRx<RX> {

    fun create(baseRxCallBack: BaseRxCallBack<RX>): Disposable {
        return Observable.create<RX> { emitter ->
            baseRxCallBack.onSubscribe(emitter)
            emitter.onComplete()
        }
            .subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread())
            .subscribe({ data: RX ->
                runOnUiThread { baseRxCallBack.onResponse(data) }
            },
                { throwable: Throwable ->
                    runOnUiThread {
                        Log.d("ads_error", throwable.stackTrace.toString())
                        baseRxCallBack.onError(throwable)
                    }
                })
    }

    fun callApi(baseRxCallBack: BaseRxCallBackCallApi<RX>): Disposable {
        return baseRxCallBack.onFun()
            .subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(
                { data: RX -> runOnUiThread { baseRxCallBack.onResponse(data) } },
                { throwable: Throwable ->
                    runOnUiThread {
                        Log.d("ads_error", throwable.stackTrace.toString())
                        baseRxCallBack.onError(throwable)
                    }
                })
    }

    interface BaseRxCallBackCallApi<RX> {

        fun onResponse(result: RX)

        fun onFun(): Observable<RX>

        fun onError(e: Throwable)
    }

    interface BaseRxCallBack<RX> {

        fun onSubscribe(emitter: ObservableEmitter<RX>)

        fun onResponse(result: RX)

        fun onError(e: Throwable)
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            Handler(Looper.getMainLooper()).post(runnable)
        }
    }
}