package com.hope_studio.video_maker_slideshow_maker.models

import com.hope_studio.video_maker_slideshow_maker.slide_show_theme.data.ThemeData

class ThemeDataModel(val themeData: ThemeData) {

    val name = themeData.themeName
    val videoPath = themeData.themeVideoFilePath
    var selected = false

}