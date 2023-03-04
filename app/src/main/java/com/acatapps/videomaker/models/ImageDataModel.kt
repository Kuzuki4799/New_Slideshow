package com.acatapps.videomaker.models

import com.acatapps.videomaker.data.ImageData

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