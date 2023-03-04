package com.acatapps.videomaker.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.graphics.drawable.toBitmap

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.acatapps.videomaker.application.VideoMakerApplication
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {
    fun getBitmapFromFilePath(filePath:String): Bitmap {
        try {
            val file = File(filePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            return modifyOrientation(bitmap, filePath)
        }catch (e:Exception) {
            FirebaseCrashlytics.getInstance().log("getBitmapFromFilePath -- $e")
            return getBlackBitmap()
        }

    }

    fun resizeBitmap(path: String?, maxSize: Int): Bitmap? {
        val bitmap: Bitmap?
        val file = File(path)
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        val fis = FileInputStream(file)
        BitmapFactory.decodeStream(fis, null, bmOptions)
        fis.close()
        var scale = 1f
        if (bmOptions.outWidth > maxSize || bmOptions.outHeight > maxSize) {
            val ratioW = bmOptions.outWidth.toFloat() / maxSize
            val ratioH = bmOptions.outHeight.toFloat() / maxSize
            val ratio = max(ratioW, ratioH)
            scale = ratio
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = scale.toInt()
        val fs = FileInputStream(file)
        bitmap = BitmapFactory.decodeStream(fs, null, options)
        fs.close()
        return bitmap
    }

    fun getStickerFromFilePath(filePath:String): Bitmap {
        val file = File(filePath)
        return BitmapFactory.decodeFile(file.absolutePath)

    }

    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotate(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotate(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotate(bitmap, 270f)
            }
            else -> {
                bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeWrapBitmap(bitmapInput: Bitmap, size:Float): Bitmap {
        if(bitmapInput.width < size && bitmapInput.height < size) return bitmapInput
        val scale = min((size/bitmapInput.width), (size/bitmapInput.height))
        return Bitmap.createScaledBitmap(bitmapInput, (bitmapInput.width*scale).toInt(), (bitmapInput.height*scale).toInt(), true)
    }

    fun resizeMatchBitmap(bitmapInput: Bitmap, size:Float): Bitmap {
        val scale = max((size/bitmapInput.width), (size/bitmapInput.height))
        return Bitmap.createScaledBitmap(bitmapInput, (bitmapInput.width*scale).toInt(), (bitmapInput.height*scale).toInt(), true)
    }



    fun blurBitmapV2(bm: Bitmap?, r: Int): Bitmap? {
        if(bm == null) return null
        val radius = 25f
        Logger.e("size = ${bm.width} x ${bm.height}")
        val rsScript: RenderScript =
            RenderScript.create(VideoMakerApplication.getContext())
        val alloc: Allocation = Allocation.createFromBitmap(rsScript, bm)
        val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
        blur.setRadius(radius)
        blur.setInput(alloc)
        val result =
            Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createTyped(rsScript, alloc.type)
        blur.forEach(outAlloc)
        outAlloc.copyTo(result)

        rsScript.destroy()
        return result
    }

    fun getBlackBitmap():Bitmap {
        val bitmap = Bitmap.createBitmap(1080,1080, Bitmap.Config.ARGB_8888)
        for(i in 0 until bitmap.width) {
            for(j in 0 until bitmap.height) {
                bitmap.setPixel(i,j, Color.BLACK)
            }
        }
        return bitmap
    }

    fun loadBitmapFromUri(path: String?, callBack: (Bitmap?) -> Unit) {
        Glide.with(VideoMakerApplication.getContext()).asBitmap().load(path).apply(
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        ).addListener(object :
            RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                callBack.invoke(null)
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                callBack.invoke(resource)
                return true
            }
        }).submit()
    }

    fun loadBitmapFromXML(id:String, callBack: (Bitmap?) -> Unit) {
        val bitmap = VideoMakerApplication.getContext().getDrawable(id.toInt())?.toBitmap(512,512)
        callBack.invoke(bitmap)
    }

    fun getBitmapFromAsset(path: String): Bitmap {
        val inputStream = VideoMakerApplication.getContext().assets.open(path)
        return BitmapFactory.decodeStream(inputStream)
    }

}