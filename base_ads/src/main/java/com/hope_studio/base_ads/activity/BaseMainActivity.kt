package com.hope_studio.base_ads.activity

import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.dialog.*
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.utils.NetWorkUtils
import com.hope_studio.base_ads.utils.ShareUtils

abstract class BaseMainActivity : BaseActivity() {

    abstract fun versionName(): String

    abstract fun emailStr(): String

    fun checkUpdate() {
        val dataAds = ShareUtils[this, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return
        when {
            dataAds.getVersionApp().toDouble() > versionName().toDouble() -> {
                DialogUpdate(dataAds.getLinkUpdate(), this).show()
            }
            dataAds.getLinkUpdate().isNotEmpty() &&
                    !NetWorkUtils.checkLinkMarket(this, dataAds.getLinkUpdate()) -> {
                DialogUpdate(dataAds.getLinkUpdate(), this).show()
            }
            else -> return
        }
    }

    override fun onBackPressed() {
        if (!ShareUtils.getBoolean(this, "rate", false)) {
            showDialogWithExit(true)
        } else {
            DialogExitApp(this).show()
        }
    }
}