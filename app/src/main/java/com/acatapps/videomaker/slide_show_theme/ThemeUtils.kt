package com.acatapps.videomaker.slide_show_theme

import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.utils.FileUtils
import java.io.File

object ThemeUtils {
    fun getThemeDataList():ArrayList<ThemeData> {
        val themeDataList = ArrayList<ThemeData>()
        themeDataList.add(ThemeData("none", ThemeData.ThemType.NOT_REPEAT, "none"))
        val themeFolder = File(FileUtils.themeFolderPath)
        if(themeFolder.exists() && themeFolder.isDirectory) {
            for(file in themeFolder.listFiles()) {
                themeDataList.add(ThemeData(file.absolutePath, ThemeData.ThemType.NOT_REPEAT, file.name))
            }
        }
        return themeDataList
    }
}