package com.hope_studio.video_maker_slideshow_maker.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ho_utils.DimenUtils

class ImageViewWithNumberCount : AppCompatImageView {

    private var mCount = 0
    private val mThemeColor = Color.parseColor("#D81B60")

    private var mBorderWidth = 6f
    private val mBorderRect = Rect()
    private val mBorderPaint = Paint()

    private var mCountRectSize = 27
    private val mCountRect = Rect()
    private val mCountRectPaint = Paint()

    private val mTextSize = 16
    private val mTextRect = Rect()
    private val mCountTextPaint = Paint()

    private var mActiveCounter = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        clipToOutline = true
        mCountRectSize = (mCountRectSize * DimenUtils.density(context)).toInt()
        mBorderWidth *= DimenUtils.density(context)
        mBorderPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mBorderWidth
            color = mThemeColor
            isAntiAlias = true
        }
        mCountRectPaint.apply {
            style = Paint.Style.FILL
            color = mThemeColor
            isAntiAlias = true
        }
        mCountTextPaint.apply {
            textSize = mTextSize * DimenUtils.density(context)
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = ResourcesCompat.getFont(context, R.font.montserrat_regular)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (mCount > 0 && mActiveCounter) {
            mBorderRect.set(0, 0, width, height)
            mCountRect.set(width - mCountRectSize, height - mCountRectSize, width, height)
            mCountTextPaint.getTextBounds(mCount.toString(), 0, mCount.toString().length, mTextRect)
            canvas?.drawRect(mBorderRect, mBorderPaint)
            canvas?.drawRect(mCountRect, mCountRectPaint)
            canvas?.drawText(
                mCount.toString(),
                width.toFloat() - mCountRectSize / 2,
                height.toFloat() - mCountRectSize / 2 + mTextRect.height() / 2,
                mCountTextPaint
            )
        }

    }

    fun increaseCount() {
        mCount++
        invalidate()
    }

    fun deincreaseCount() {
        mCount--
        if (mCount < 0) mCount = 0
        invalidate()
    }

    fun setCount(count: Int) {
        mCount = count

        invalidate()

    }

    fun getCount(): Int = mCount

    fun disableCounter() {
        mActiveCounter = false
        invalidate()
    }

    fun activeCounter() {
        mActiveCounter = true
        invalidate()
    }

}