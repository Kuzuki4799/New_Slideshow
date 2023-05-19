package com.hope_studio.video_maker_slideshow_maker.ui.pick_media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hope_studio.video_maker_slideshow_maker.data.MediaData
import com.hope_studio.video_maker_slideshow_maker.models.MediaDataModel
import com.hope_studio.video_maker_slideshow_maker.models.MediaPickedDataModel
import com.hope_studio.video_maker_slideshow_maker.modules.local_storage.LocalStorageData

class PickMediaViewModel (val localStorageData: LocalStorageData) : ViewModel() {

    private val mItemJustPicked = MutableLiveData<MediaDataModel>()
    val itemJustPicked get() = mItemJustPicked

    private val mItemJustDeleted = MutableLiveData<MediaPickedDataModel>()
    val itemJustDeleted get() = mItemJustDeleted

    private val mActiveCounter = MutableLiveData<Boolean>(true)
    val acctiveCounter get() = mActiveCounter

    private val mNewMediaItem = MutableLiveData<MediaData>()
    val newMediaItem get() = mNewMediaItem

    private val mFolderIsShowing = MutableLiveData<Boolean>(false)
    val folderIsShowingLiveData get() = mFolderIsShowing

    var folderIsShowing = false

    private val mMediaPickedCount = HashMap<String, Int>()
    val mediaPickedCount get() = mMediaPickedCount


    fun onShowFolder() {
        folderIsShowing = true
    }

    fun hideFolder() {
        folderIsShowing = false
        mFolderIsShowing.postValue(false)
    }

    fun onPickImage(mediaDataModel: MediaDataModel) {
        var count = mMediaPickedCount[mediaDataModel.filePath] ?: 0
        mMediaPickedCount[mediaDataModel.filePath]=count+1
        mItemJustPicked.postValue(mediaDataModel)
    }
    fun updateCount(pathList:ArrayList<String>) {
        for(path in pathList) {
            var count = mMediaPickedCount[path] ?: 0
            mMediaPickedCount[path]=count+1
        }

    }


    fun onDelete(mediaDataModel: MediaPickedDataModel) {
        var count = mMediaPickedCount[mediaDataModel.path] ?: 0
        mMediaPickedCount[mediaDataModel.path]=count-1
        mItemJustDeleted.postValue(mediaDataModel)
    }

    fun disableCounter() {
        mActiveCounter.postValue(false)
    }

    fun addNewMediaData(mediaData:MediaData) {
        mNewMediaItem.postValue(mediaData)
    }



}