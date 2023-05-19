package com.hope_studio.base_ads.utils

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.android.billingclient.api.*
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.model.DataModel

object BillingUtils {

    enum class Premium(val value: String) {
        MONTHLY("month"), YEARLY("yearly"), FOREVER("premium")
    }

    private var isBilling = false
    private const val BILLING = "BILLING"
    private var billingClient: BillingClient? = null

    var onPurchaseCallback: OnPurchaseCallback? = null

    interface OnPurchaseCallback {
        fun onPurchaseListener()
    }

    fun setDataBilling(context: Context, isBilling: Boolean) {
        ShareUtils.putBoolean(context, BILLING, isBilling)
    }

    fun getDataBilling(context: Context): Boolean {
        return ShareUtils.getBoolean(context, BILLING, false)
    }

    fun showHideBilling(context: BaseActivity, frame: View) {
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        when {
            dataAds == null -> frame.visibility = View.GONE
            !dataAds.getPlayBilling() -> frame.visibility = View.GONE
            else -> {
                if (getDataBilling(context)) {
                    frame.visibility = View.GONE
                } else {
                    frame.visibility = View.VISIBLE
                }
            }
        }
    }

    fun showHideBillingCheckServer(context: BaseActivity, frame: View) {
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        when {
            dataAds == null -> frame.visibility = View.GONE
            !dataAds.getPlayBilling() -> frame.visibility = View.GONE
        }
    }

    fun checkBilling(context: BaseActivity, frame: RelativeLayout, listener: () -> Unit) {
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        when {
            dataAds == null -> frame.visibility = View.INVISIBLE
            !dataAds.getPlayBilling() -> frame.visibility = View.INVISIBLE
            else -> {
                frame.visibility = View.VISIBLE
                frame.setOnClickListener { listener.invoke() }
            }
        }
    }

    fun checkBilling(context: BaseActivity, frame: RelativeLayout) {
        val dataAds = ShareUtils[context, DataModel::class.java.name, DataModel::class.java]
        when {
            dataAds == null -> frame.visibility = View.INVISIBLE
            !dataAds.getPlayBilling() -> frame.visibility = View.INVISIBLE
            else -> {
                frame.visibility = View.VISIBLE
            }
        }
    }

    fun onResumeSubs(context: BaseActivity, listener: () -> Unit) {
        billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }

                    for (purchase in list) {
                        for (i in purchase.skus) {
                            if (i.equals(Premium.MONTHLY.value)) {
                                ShareUtils.putBoolean(context, Premium.MONTHLY.value, true)
                                listener.invoke()
                            }
                            if (i.equals(Premium.YEARLY.value)) {
                                ShareUtils.putBoolean(context, Premium.YEARLY.value, true)
                                listener.invoke()
                            }

                            if (i == purchase.skus[purchase.skus.size - 1]) {
                                val month = ShareUtils.getBoolean(
                                    context, Premium.MONTHLY.value, false
                                )
                                val year = ShareUtils.getBoolean(
                                    context, Premium.YEARLY.value, false
                                )
                                val forever = ShareUtils.getBoolean(
                                    context, Premium.FOREVER.value, false
                                )

                                if (month || year || forever) {
//                                                setDataBilling(context, true)
                                } else {
                                    setDataBilling(context, false)
                                }
                            }
                        }

                    }
                }
            }
        }

        billingClient?.queryPurchasesAsync(BillingClient.SkuType.INAPP) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList.size > 0
            ) {
                for (purchase in purchaseList) {
                    for (i in purchase.skus) {
                        if (i.equals(Premium.FOREVER.value)) {
                            ShareUtils.putBoolean(context, Premium.FOREVER.value, true)
                            listener.invoke()
                        }

                        if (i == purchase.skus[purchase.skus.size - 1]) {
                            val month = ShareUtils.getBoolean(
                                context, Premium.MONTHLY.value, false
                            )
                            val year = ShareUtils.getBoolean(
                                context, Premium.YEARLY.value, false
                            )
                            val forever = ShareUtils.getBoolean(
                                context, Premium.FOREVER.value, false
                            )

                            if (month || year || forever) {
//                                                setDataBilling(context, true)
                            } else {
                                setDataBilling(context, false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setupBilling(context: BaseActivity) {
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases()
            .setListener { billingResult, purchases ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        if (onPurchaseCallback != null) {
                            onPurchaseCallback?.onPurchaseListener()
                        }
                    }
                    BillingClient.BillingResponseCode.OK -> if (purchases != null) {
                        for (ignored in purchases) verifySubPurchase(ignored)
                    }
                }
            }.build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isBilling = true

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showProducts(context, false)
                }
            }

            override fun onBillingServiceDisconnected() {
                isBilling = false
            }
        })
    }

    private fun verifySubPurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken).build()
            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
                if (onPurchaseCallback != null) {
                    onPurchaseCallback?.onPurchaseListener()
                }
            }
        }
    }

    fun showProducts(context: BaseActivity, in_app: Boolean) {
        try {
            if (!isBilling) return
            val productIds = ArrayList<String>()
            val skuType = if (in_app) {
                BillingClient.SkuType.INAPP
            } else {
                BillingClient.SkuType.SUBS
            }

            if (in_app) {
                productIds.add(Premium.FOREVER.value)
            } else {
                productIds.add(Premium.MONTHLY.value)
                productIds.add(Premium.YEARLY.value)
            }

            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(productIds).setType(skuType).build()

            billingClient?.querySkuDetailsAsync(skuDetailsParams) { _: BillingResult?, list: List<SkuDetails?>? ->
                if (list != null) {
                    val listSku = ShareUtils.getArrayGson(context, SkuDetails::class.java)
                    for (i in list) {
                        i?.let { listSku.add(it) }
                    }
                    ShareUtils.putArrayGson(context, SkuDetails::class.java.name, listSku)

                    if (in_app) {
                        billingClient?.queryPurchasesAsync(BillingClient.SkuType.INAPP) { billingResult, purchaseList ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList.size > 0
                            ) {
                                for (purchase in purchaseList) {
                                    for (i in purchase.skus) {
                                        if (i.equals(Premium.FOREVER.value)) {
                                            ShareUtils.putBoolean(
                                                context, Premium.FOREVER.value, true
                                            )
//                                            setDataBilling(context, true)
                                        }

                                        if (i == purchase.skus[purchase.skus.size - 1]) {
                                            val month = ShareUtils.getBoolean(
                                                context, Premium.MONTHLY.value, false
                                            )
                                            val year = ShareUtils.getBoolean(
                                                context, Premium.YEARLY.value, false
                                            )
                                            val forever = ShareUtils.getBoolean(
                                                context, Premium.FOREVER.value, false
                                            )

                                            if (month || year || forever) {
//                                                setDataBilling(context, true)
                                            } else {
                                                setDataBilling(context, false)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, list ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                for (purchase in list) {
                                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                                        verifySubPurchase(purchase)
                                    }

                                    for (purchase in list) {
                                        for (i in purchase.skus) {
                                            when (i) {
                                                Premium.MONTHLY.value -> {
                                                    ShareUtils.putBoolean(
                                                        context, Premium.MONTHLY.value, true
                                                    )
//                                                    setDataBilling(context, true)
                                                }

                                                Premium.YEARLY.value -> {
                                                    ShareUtils.putBoolean(
                                                        context, Premium.YEARLY.value, true
                                                    )
//                                                    setDataBilling(context, true)
                                                }
                                            }
                                            if (i == purchase.skus[purchase.skus.size - 1]) {
                                                val month = ShareUtils.getBoolean(
                                                    context, Premium.MONTHLY.value, false
                                                )
                                                val year = ShareUtils.getBoolean(
                                                    context, Premium.YEARLY.value, false
                                                )
                                                val forever = ShareUtils.getBoolean(
                                                    context, Premium.FOREVER.value, false
                                                )

                                                if (month || year || forever) {
//                                                    setDataBilling(context, true)
                                                } else {
                                                    setDataBilling(context, false)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        showProducts(context, true)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error Purchase", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchPurchaseFlow(context: BaseActivity, skuDetails: SkuDetails?) {
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
            Handler().postDelayed({
                if (onPurchaseCallback != null) {
                    onPurchaseCallback?.onPurchaseListener()
                }
            }, 500)
        } else {
            val billingFlowParams =
                skuDetails?.let { BillingFlowParams.newBuilder().setSkuDetails(it).build() }
            billingFlowParams?.let { billingClient?.launchBillingFlow(context, it) }
        }
    }
}