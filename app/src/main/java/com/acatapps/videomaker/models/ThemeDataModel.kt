package com.acatapps.videomaker.models

import com.acatapps.videomaker.slide_show_theme.data.ThemeData

class ThemeDataModel(val themeData: ThemeData) {

    val name = themeData.themeName
    val videoPath = themeData.themeVideoFilePath
    var selected = false

}