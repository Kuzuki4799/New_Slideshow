package com.hope_studio.video_maker_slideshow_maker.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.ui.pick_media.MediaFolderFragment
import com.hope_studio.video_maker_slideshow_maker.ui.pick_media.MediaListFragment

class PickMediaPagerAdapter(val context: Context, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MediaFolderFragment()
            }
            else -> {
                MediaListFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> {
                context.getString(R.string.albums).toUpperCase()
            }
            else -> {
                context.getString(R.string.gallery).toUpperCase()
            }
        }
    }

    override fun getCount(): Int = 1
}