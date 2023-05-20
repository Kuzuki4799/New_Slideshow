package com.hope_studio.video_maker_slideshow_maker.data

import com.hope_studio.video_maker_slideshow_maker.ho_enum_.MediaKind

data class MediaData(val dateAdded:Long, val filePath:String = "", val fileName:String="", val mediaKind: MediaKind=MediaKind.PHOTO, val folderId:String="", val folderName:String="", val duration:Long=0)