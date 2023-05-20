package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.ImageData

class ImageDataModel {
    val filePath:String
    var count = 0
    val dateAdded:Long
    constructor(imageData: ImageData) {
        this.filePath = imageData.filePath
        this.dateAdded = imageData.dateAdded
    }

    constructor(dateAdded:Long) {
        this.dateAdded = dateAdded
        filePath = ""
    }
}