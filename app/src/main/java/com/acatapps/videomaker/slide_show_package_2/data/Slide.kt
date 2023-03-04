package com.acatapps.videomaker.slide_show_package_2.data


class Slide(private val mSlideId:Int, private val mImagePath:String) {

    private var mDelayTime = 3
    private var mTransitionTime = 2

    val slideId get() = mSlideId

    val imagePath get() = mImagePath

}