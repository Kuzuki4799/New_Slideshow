package com.acatapps.videomaker.image_slide_show.drawer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import java.io.File
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

class ImageSlideDataContainer( val mImageList: ArrayList<String> = ArrayList()) {

    private val mImageSlideDataList = ArrayList<ImageSlideData>()
    @Volatile
    var delayTimeMs = 2500

     val transitionTimeMs = 500
    private val fps = 25
    val imageList get() = mImageList
    private var mCurrentSlideIndex = 0

    @Volatile
    private lateinit var mCurrentBitmap: Bitmap
    @Volatile
    private lateinit var mNextBitmap: Bitmap
    @Volatile
    private lateinit var mBackupBitmap: Bitmap

    private var mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/NONE.jpg")
    private var mToLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/NONE.jpg")

    init {
        initData()
    }

    fun initData() {
        mImageSlideDataList.clear()
        if(mImageList.size > 0) {
            for (index in 0 until mImageList.size) {
                val nextImagePath = if (index < mImageList.size - 1) {
                    mImageList[index + 1]
                } else {
                    mImageList[mImageList.size-1]
                }
                val imageSlideData =
                    ImageSlideData(View.generateViewId()+System.currentTimeMillis(), mImageList[index], nextImagePath)
                mImageSlideDataList.add(imageSlideData)
            }

            val firstSlide = mImageSlideDataList[0]
            mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
            mCurrentSlideId = firstSlide.slideId
            val secondSlide = mImageSlideDataList[1]
            mNextBitmap = getBitmapResized(secondSlide.fromImagePath)

        }
        updateBackupSlide()

    }

    private val noneLutPath = "lut/NONE.jpg"
    fun onRepeat() {
        mCurrentSlideId = 1L
        val firstSlide = mImageSlideDataList[0]
        mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
        mNextBitmap = getBitmapResized(firstSlide.toImagePath)
        mCurrentSlideId = firstSlide.slideId
        mCurrentSlideIndex = 0
        mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex].lookupType}.jpg")

        mToLookupBitmap = if (mCurrentSlideIndex < mImageSlideDataList.size - 1)
            BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex+1].lookupType}.jpg")
        else BitmapUtils.getBitmapFromAsset(noneLutPath)

    }

    private fun getBitmapResized(path: String): Bitmap {
        val imageFile = File(path)
        return if (imageFile.exists()) {
            val outName = "${imageFile.parentFile?.name}${imageFile.name}"
            val tempImageFile = File("${FileUtils.tempImageFolderPath}/$outName")
            if (tempImageFile.exists()) {
                BitmapUtils.getBitmapFromFilePath(tempImageFile.absolutePath)
            } else {
                val resizedBitmap = drawResizedBitmap(path)
                FileUtils.saveBitmapToTempData(resizedBitmap, outName)
                resizedBitmap
            }
        } else {
            BitmapUtils.getBlackBitmap()
        }


    }

    private var mCurrentSlideId = 1L
    fun getFrameDataByTime(timeMs: Int, needReload:Boolean=false): ImageSlideFrame {

        var slideIndex = ((timeMs) / (delayTimeMs + transitionTimeMs))
        if(slideIndex == -1) slideIndex = mImageSlideDataList.size-1
        val targetSlide = mImageSlideDataList[slideIndex]
        mCurrentSlideIndex = slideIndex

        val delta = timeMs-slideIndex*(delayTimeMs+transitionTimeMs)
        var progress:Float
        progress = when {
            delta in 0..delayTimeMs -> 0f
            slideIndex == mImageSlideDataList.size-1 -> 0f
            else -> {
                ((delta-delayTimeMs).toFloat()/transitionTimeMs)
            }
        }

        calculateZoom(timeMs)

        if (needReload || targetSlide.slideId !=mCurrentSlideId) {
            if(targetSlide.slideId !=mCurrentSlideId) {
                val f = zoom
                zoom= zoom1
                zoom1=f

                val f2 =zoomF
                zoomF = zoomT
                zoomT=f2
            }
            mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex].lookupType}.jpg")

            mToLookupBitmap = if (mCurrentSlideIndex < mImageSlideDataList.size - 1)
                BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex+1].lookupType}.jpg")
            else BitmapUtils.getBitmapFromAsset(noneLutPath)

            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId

        }
        return ImageSlideFrame(mCurrentBitmap, mNextBitmap, mFromLookupBitmap,mToLookupBitmap,progress, mCurrentSlideId, zoom,zoom1)
    }

    private var zoom = 1f
    private var zoom1 = 1f

    private val zoomDuration = 5000

    private var zoomF = 1f
    private var zoomT = 0.95f

    private fun calculateZoom(timeMs:Int) {


    }
    fun seekTo(timeMs: Int, needReload: Boolean=false): ImageSlideFrame {

        var slideIndex = ((timeMs) / (delayTimeMs + transitionTimeMs))

        if (slideIndex == mImageSlideDataList.size) slideIndex = 0
        mCurrentSlideIndex = slideIndex
        val targetSlide = mImageSlideDataList[slideIndex]

        val surplus = timeMs  % (delayTimeMs + transitionTimeMs)
        val progress: Float
        progress = if (surplus <= delayTimeMs) {
            0f
        } else {
            (surplus  - delayTimeMs) / (transitionTimeMs.toFloat())
        }

        calculateZoom(timeMs)
        if (needReload || targetSlide.slideId != mCurrentSlideId) {

            mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex].lookupType}.jpg")
            mToLookupBitmap = if (mCurrentSlideIndex < mImageSlideDataList.size - 1)
                BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex+1].lookupType}.jpg")
            else
                BitmapUtils.getBitmapFromAsset(noneLutPath)
            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId

        }
        return ImageSlideFrame(mCurrentBitmap, mNextBitmap,mFromLookupBitmap,mToLookupBitmap, progress,mCurrentSlideId, zoom, zoom1)
    }

    private fun updateBackupSlide() {
        Thread {

                for (index in 0 until mImageSlideDataList.size) {
                    val item = mImageSlideDataList[index]


                        getBitmapResized(item.fromImagePath)


                }


        }.start()
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

    fun getMaxDurationMs(): Int {
        return (delayTimeMs + transitionTimeMs) * mImageSlideDataList.size
    }

    fun getStartTimeById(slideId:Long):Int {

        for(index in 0 until mImageSlideDataList.size) {
            val item = mImageSlideDataList[index]
            if(slideId == item.slideId) {
                return index*(delayTimeMs+transitionTimeMs)
            }
        }
        return 0
    }

    fun getCurrentDelayTimeMs(): Int = delayTimeMs

    fun getSlideList():ArrayList<ImageSlideData> = mImageSlideDataList

    fun prepareForRender(imageSlideDataList:ArrayList<ImageSlideData>, delayTime:Int) {
        mImageSlideDataList.clear()
        mImageSlideDataList.addAll(imageSlideDataList)
        mCurrentSlideIndex = 0
        this.delayTimeMs = delayTime
        val firstSlide = mImageSlideDataList[0]
        mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
        mNextBitmap = getBitmapResized(firstSlide.toImagePath)
        mCurrentSlideId = firstSlide.slideId
        val secondSlide = mImageSlideDataList[1]
        mBackupBitmap = getBitmapResized(secondSlide.toImagePath)

        mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex].lookupType}.jpg")

        mToLookupBitmap = if (mCurrentSlideIndex < mImageSlideDataList.size - 1)
            BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex+1].lookupType}.jpg")
        else BitmapUtils.getBitmapFromAsset(noneLutPath)
    }

    fun getFrameByTimeForRender(timeMs:Int):ImageSlideFrame {
        var slideIndex = ((timeMs - 1) / (delayTimeMs + transitionTimeMs))

        if (slideIndex == mImageSlideDataList.size) slideIndex--
        val targetSlide = mImageSlideDataList[slideIndex]
        mCurrentSlideIndex = slideIndex
        val surplus = (timeMs - 1) % (delayTimeMs + transitionTimeMs)
        val progress: Float
        progress = if (surplus <= delayTimeMs) {
            0f
        } else {
            (surplus + 1 - delayTimeMs) / transitionTimeMs.toFloat()
        }
        calculateZoom(timeMs)
        if (targetSlide.slideId != mCurrentSlideId) {

                val f = zoom
                zoom= zoom1
                zoom1=f

                val f2 =zoomF
                zoomF = zoomT
                zoomT=f2

            mFromLookupBitmap = BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex].lookupType}.jpg")

            mToLookupBitmap = if (mCurrentSlideIndex < mImageSlideDataList.size - 1)
                BitmapUtils.getBitmapFromAsset("lut/${mImageSlideDataList[mCurrentSlideIndex+1].lookupType}.jpg")
            else BitmapUtils.getBitmapFromAsset(noneLutPath)

            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId


        }

        return ImageSlideFrame(mCurrentBitmap, mNextBitmap, mFromLookupBitmap,mToLookupBitmap,progress, mCurrentSlideId, zoom,zoom1)
    }

    fun setNewImageList(pathList:ArrayList<String>) {
        mImageList.clear()
        mImageList.addAll(pathList)
        initData()
    }

    fun changeCurrentSlideId(id:Long) {
        mCurrentSlideId = id
    }
}