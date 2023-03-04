package com.acatapps.videomaker.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.acatapps.videomaker.R
import com.acatapps.videomaker.ui.pick_media.MediaFolderFragment
import com.acatapps.videomaker.ui.pick_media.MediaListFragment

class PickMediaPagerAdapter(val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->{
                MediaListFragment()
            }
            else ->{
                MediaFolderFragment()
            }

        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0->{
                context.getString(R.string.gallery).toUpperCase()
            }
            else -> {
                context.getString(R.string.albums).toUpperCase()
            }
        }
    }

    override fun getCount(): Int = 2

}