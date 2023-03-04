package com.acatapps.videomaker.slide_show_package_2

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.acatapps.videomaker.slide_show_package_2.data.FrameData
import com.acatapps.videomaker.slide_show_package_2.data.Slide
import com.acatapps.videomaker.slide_show_package_2.data.SlideRenderData
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import kotlin.math.max

class SlideShowForRender (private val mImageList:ArrayList<String>, private val mBitmapHashMap: HashMap<String, String>, private val mDelayTimeMilSec:Int) {

    private var mCurrentSlide: SlideRenderData? = null
    private var mNextSlide: SlideRenderData? = null
    private lateinit var mCurrentSlideData:Slide
    private lateinit var mNextSlideData:Slide
    private val mTransitionTime = 2000
    private val mTimePerSlide = mDelayTimeMilSec+mTransitionTime
    private var mCurrentSlideIndex = -1
    val totalDuration = mTimePerSlide*mImageList.size
    val bitmap = BitmapUtils.getBitmapFromFilePath(mImageList[0])
    init {
        prepareImage()
    }
    fun getFrameByVideoTimeForRender(timeMiniSec:Int): FrameData? {
        var targetSlideIndex = (timeMiniSec)/(mTimePerSlide)

        val surplus = timeMiniSec-(targetSlideIndex*mTimePerSlide)
        val progress:Float
        if(surplus <= mDelayTimeMilSec) {
            progress = 0f
        } else {
            progress = (surplus - mDelayTimeMilSec)/mTransitionTime.toFloat()
        }

        val zoom = 1f

        if(targetSlideIndex <= 0) targetSlideIndex = 0
        else if(targetSlideIndex >= mImageList.size-1) targetSlideIndex = mImageList.size-1

        if(mCurrentSlideIndex == targetSlideIndex) {
            return FrameData(mCurrentSlide!!.imageBitmap, mNextSlide!!.imageBitmap, progress, mCurrentSlide!!.slide.slideId, zoom)
        } else {
            mCurrentSlideIndex = targetSlideIndex
            if(mNextSlide == null && targetSlideIndex == 0) {
                mCurrentSlideData = Slide( View.generateViewId(), mImageList[targetSlideIndex])
                mCurrentSlide = SlideRenderData(mCurrentSlideData, getBitmapFromHashMapV2(mImageList[targetSlideIndex]))

                mNextSlideData = Slide(View.generateViewId(), mImageList[targetSlideIndex+1])
                mNextSlide = SlideRenderData(mNextSlideData, getBitmapFromHashMapV2(mImageList[targetSlideIndex+1]))
            } else {
                mCurrentSlide = mNextSlide
                mNextSlideData = Slide(View.generateViewId(), mImageList[targetSlideIndex])
                mNextSlide = SlideRenderData(mNextSlideData, getBitmapFromHashMapV2(mImageList[targetSlideIndex]))
            }
            return FrameData(mCurrentSlide!!.imageBitmap, mNextSlide!!.imageBitmap, progress, mCurrentSlide!!.slide.slideId, zoom)
        }

    }

    private fun prepareImage() {
        Thread{
            for(index in 0 until mImageList.size) {
                val path = mImageList[index]
                Logger.e("prepare index = $index")
                if(mBitmapHashMap[path] == null) {
                    val bitmap = drawResizedBitmap(path)
                    val outFilePath = FileUtils.saveBitmapToTempData(bitmap)
                    mBitmapHashMap[path] = outFilePath
                }
            }
        }.start()
    }

    private fun getBitmapFromHashMapV2(imagePath: String): Bitmap {
        return if(mBitmapHashMap[imagePath] == null) {
            val bitmap = drawResizedBitmap(imagePath)
            val outFilePath = FileUtils.saveBitmapToTempData(bitmap)
            mBitmapHashMap[imagePath] = outFilePath
            return bitmap
        } else {
            BitmapUtils.getBitmapFromFilePath(mBitmapHashMap[imagePath]!!)
        }
    }
    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val screenW = 1080
        val rawBitmap = BitmapUtils.getBitmapFromFilePath(imagePath)
        val outBitmapSize: Int
        if (rawBitmap.width < screenW && rawBitmap.height < screenW) {
            outBitmapSize = max(rawBitmap.width, rawBitmap.height)
        } else {
            outBitmapSize = screenW
        }

        val blurBgBitmap = BitmapUtils.blurBitmapV2(
            BitmapUtils.resizeMatchBitmap(
                rawBitmap,
                outBitmapSize.toFloat() + 100
            ), 20
        )

        val rawBitmapResized = BitmapUtils.resizeWrapBitmap(rawBitmap, outBitmapSize.toFloat())

        val resizedBitmapWithBg =
            Bitmap.createBitmap(outBitmapSize, outBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            blurBgBitmap?.let {
                drawBitmap(
                    it,
                    (outBitmapSize - it.width) / 2f,
                    (outBitmapSize - it.height) / 2f,
                    null
                )
            }
            drawBitmap(
                rawBitmapResized,
                (outBitmapSize - rawBitmapResized.width) / 2f,
                (outBitmapSize - rawBitmapResized.height) / 2f,
                null
            )
        }

        return resizedBitmapWithBg
    }
    private fun getBlackSlide() :SlideRenderData{
        return SlideRenderData(Slide(View.generateViewId(), "none"), BitmapUtils.getBlackBitmap())
    }


}