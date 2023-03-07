package com.hope_studio.video_maker_slideshow_maker.models

import com.hope_studio.video_maker_slideshow_maker.data.MediaData

class MediaAlbumDataModel {
    val mediaItemPaths = arrayListOf<MediaData>()
    val folderId:String
    val albumName:String

    constructor(albumName:String, folderId:String) {
        this.folderId = folderId
        this.albumName = albumName
    }

}