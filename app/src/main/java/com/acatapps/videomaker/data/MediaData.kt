package com.acatapps.videomaker.data

import com.acatapps.videomaker.enum_.MediaKind

data class MediaData(val dateAdded:Long, val filePath:String = "", val fileName:String="", val mediaKind: MediaKind=MediaKind.PHOTO, val folderId:String="", val folderName:String="", val duration:Long=0)