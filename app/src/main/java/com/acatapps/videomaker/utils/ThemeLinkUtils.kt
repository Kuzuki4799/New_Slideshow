package com.acatapps.videomaker.utils

import com.acatapps.videomaker.data.ThemeLinkData

object ThemeLinkUtils {



    val linkThemeList = ArrayList<ThemeLinkData>().apply {
        add(ThemeLinkData("PUT_THEME_FILE_URL_HERE", "theme_boom_shape","Boom Shape"))
        add(ThemeLinkData("PUT_THEME_FILE_URL_HERE", "theme_balloon","Ballon"))
        add(ThemeLinkData("PUT_THEME_FILE_URL_HERE", "green_chrismas","Green christmas"))
        add(ThemeLinkData("PUT_THEME_FILE_URL_HERE", "theme_birthday","Birthday"))
    }
}