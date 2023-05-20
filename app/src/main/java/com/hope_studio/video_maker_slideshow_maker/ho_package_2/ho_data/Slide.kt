package com.hope_studio.video_maker_slideshow_maker.ho_package_2.ho_data


class Slide(private val mSlideId:Int, private val mImagePath:String) {

    private var mDelayTime = 3
    private var mTransitionTime = 2

    val slideId get() = mSlideId

    val imagePath get() = mImagePath

}