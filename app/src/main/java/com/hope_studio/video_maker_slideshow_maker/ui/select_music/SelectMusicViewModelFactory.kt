package com.hope_studio.video_maker_slideshow_maker.ui.select_music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hope_studio.video_maker_slideshow_maker.modules.local_storage.LocalStorageData

class SelectMusicViewModelFactory (private val localStorageData: LocalStorageData) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectMusicViewModel(localStorageData) as T
    }

}