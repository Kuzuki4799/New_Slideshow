package com.hope_studio.video_maker_slideshow_maker.custom_view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.hope_studio.video_maker_slideshow_maker.utils.DimenUtils

class CornerImageView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs) {
    private var mCorner = 6f
    private fun getClipPath(): Path {
        val cornerRadius = mCorner*DimenUtils.density(context)
        val path = Path()
        path.reset()
        path.addRoundRect(RectF(0f,0f,width.toFloat(), height.toFloat()), cornerRadius, cornerRadius, Path.Direction.CW)
        path.close()
        return path
    }

    fun changeCorner(corner:Float) {
        mCorner = corner
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.clipPath(getClipPath())
        super.onDraw(canvas)
    }


}