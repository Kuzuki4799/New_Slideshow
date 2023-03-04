package com.acatapps.videomaker.models

import android.graphics.Bitmap
import java.io.Serializable

class StickerAddedDataModel(val bitmap: Bitmap, var inEdit:Boolean, var startTimeMilSec:Int, var endTimeMilSec:Int, val stickerViewId:Int):Serializable {


}