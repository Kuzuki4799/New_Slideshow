package com.hope_studio.base_ads.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import com.blankj.utilcode.util.ScreenUtils
import com.hope_studio.base_ads.R
import com.hope_studio.base_ads.base.BaseActivity
import kotlinx.android.synthetic.main.activity_finish.*

class Finish2Activity : BaseActivity(){

    private var height = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_finish2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish2)
        animatedView.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    animatedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    height = animatedView.measuredHeight
                }
            })

        Handler().postDelayed({
            animatedView.y =
                (ScreenUtils.getScreenHeight() / 2 - ScreenUtils.getScreenHeight() / 4).toFloat()
            animatedView.invalidate()
            faceIn()
        }, 200)
    }

    private fun faceIn() {
        animatedView.visibility = View.VISIBLE
        val faceIn = ObjectAnimator.ofFloat(animatedView, "alpha", 0f, 1f)
        faceIn.duration = 800
        val mAnimationSet = AnimatorSet()
        mAnimationSet.play(faceIn)
        mAnimationSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                moveCenter()
            }
        })
        mAnimationSet.start()
    }

    private fun faceOut(view: View) {
        val faceOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        faceOut.duration = 800
        val mAnimationSet = AnimatorSet()
        mAnimationSet.play(faceOut)
        mAnimationSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (!isFinishing) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask()
                    } else {
                        finish()
                    }
                    overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit)
                }
            }
        })
        mAnimationSet.start()
    }

    private fun moveCenter() {
        val animSet = AnimatorSet()
        animSet.play(
            animationMoveY(
                animatedView,
                (ScreenUtils.getScreenHeight() / 2 - ScreenUtils.getScreenHeight() / 4).toFloat(),
                (ScreenUtils.getScreenHeight() / 2 - height / 2).toFloat()
            )
        )
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                faceOut(animatedView)
            }
        })
        animSet.start()
    }

    private fun animationMoveY(view: View?, Y1: Float, Y2: Float): ObjectAnimator? {
        val translationUp = ObjectAnimator.ofFloat(view, "Y", Y1, Y2)
        translationUp.interpolator = LinearInterpolator()
        translationUp.duration = 800
        return translationUp
    }
}