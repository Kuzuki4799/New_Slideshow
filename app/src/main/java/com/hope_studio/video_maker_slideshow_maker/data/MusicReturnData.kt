package com.hope_studio.video_maker_slideshow_maker.data

import java.io.Serializable

class MusicReturnData(val audioFilePath:String,var outFilePath:String="",val startOffset:Int,val length:Int):Serializable