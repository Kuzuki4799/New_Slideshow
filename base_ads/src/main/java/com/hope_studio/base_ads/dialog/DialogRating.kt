package com.hope_studio.base_ads.dialog

import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.KeyboardUtils
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseDialog
import com.hope_studio.base_ads.utils.AnalyticsUtils
import com.hope_studio.base_ads.utils.NetWorkUtils
import com.hope_studio.base_ads.utils.ShareUtils
import com.hope_studio.base_ads.utils.Utils
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.base.BaseActivity

class DialogRating(val activity: BaseActivity, private val emailStr: String) :
    BaseDialog(activity, Gravity.CENTER, true) {

    private var isRatting = true
    private var isExit = false

    private var ratingBar: RatingBar
    private var title: AppCompatTextView
    private var message: AppCompatTextView
    private var edFeedback: AppCompatEditText

    override fun getLayoutId(): Int {
        return R.layout.dialog_rating
    }

    init {
        val txtNo = (dialogPlus?.findViewById(R.id.txtNo) as AppCompatTextView)
        val txtYes = (dialogPlus?.findViewById(R.id.txtYes) as AppCompatTextView)
        message = (dialogPlus?.findViewById(R.id.message) as AppCompatTextView)
        title = (dialogPlus?.findViewById(R.id.title) as AppCompatTextView)
        ratingBar = (dialogPlus?.findViewById(R.id.rb_stars) as RatingBar)
        edFeedback = (dialogPlus?.findViewById(R.id.edFeedback) as AppCompatEditText)

        edFeedback.imeOptions = EditorInfo.IME_ACTION_DONE
        edFeedback.setRawInputType(InputType.TYPE_CLASS_TEXT)

        edFeedback.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentEmail(emailStr)
                true
            } else false
        }

        txtYes.setOnClickListener {
            if (isRatting) {
                when (ratingBar.progress) {
                    0 -> {
                        Toast.makeText(
                            activity, activity.getString(R.string.pls_rate), Toast.LENGTH_SHORT
                        ).show()
                    }
                    4 -> {
                        if (!com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            AnalyticsUtils.pushEventAnalytic("rate_app", null)
                            ShareUtils.putBoolean(activity, "rate", true)
                        }
                        NetWorkUtils.intentToChPlay(activity)
                        dismiss()
                    }
                    5 -> {
                        if (!com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            AnalyticsUtils.pushEventAnalytic("rate_app", null)
                            ShareUtils.putBoolean(activity, "rate", true)
                        }
                        NetWorkUtils.intentToChPlay(activity)
                        dismiss()
                    }
                    else -> {
                        isRatting = false
                        message.visibility = View.GONE
                        ratingBar.visibility = View.GONE
                        edFeedback.visibility = View.VISIBLE
                        KeyboardUtils.showSoftInput(edFeedback)
                        title.text = activity.getString(R.string.feedback)
                    }
                }
            } else intentEmail(emailStr)
        }

        txtNo.setOnClickListener {
            if (isExit) {
                BaseAds.showInterstitialAdExit(activity)
                isExit = false
            }
            dismiss()
        }
    }

    private fun intentEmail(emailStr: String) {
        if (edFeedback.text.toString().isNotEmpty()) {
            KeyboardUtils.hideSoftInput(edFeedback)
            Utils.handleSendEmail(
                activity, emailStr, ratingBar.progress, edFeedback.text.toString()
            )
            dismiss()
        } else {
            Toast.makeText(
                activity, activity.getString(R.string.enter_your_feedback), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setExit(isExit: Boolean) {
        this.isExit = isExit
    }


    override fun show() {
        isRatting = true
        edFeedback.setText("")
        ratingBar.progress = 0
        message.visibility = View.VISIBLE
        ratingBar.visibility = View.VISIBLE
        edFeedback.visibility = View.GONE
        title.text = activity.getString(R.string.rate_app)
        super.show()
    }
}