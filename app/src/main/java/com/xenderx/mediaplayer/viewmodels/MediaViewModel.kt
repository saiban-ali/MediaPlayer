package com.xenderx.mediaplayer.viewmodels

import android.support.v4.media.session.MediaControllerCompat
import android.widget.MediaController
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MediaViewModel : ViewModel() {

    private val mMediaTitleLiveData: MutableLiveData<String> = MutableLiveData()
    val mediaTitleLiveData: LiveData<String>
        get() = mMediaTitleLiveData

    private val mIsMediaPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isMediaPlaying: LiveData<Boolean>
        get() = mIsMediaPlaying

    private val mMediaProgress: MutableLiveData<Long> = MutableLiveData()
    val mediaProgress: LiveData<Long>
        get() = mMediaProgress

    private val mMediaDuration: MutableLiveData<Long> = MutableLiveData()
    val mediaDuration: LiveData<Long>
        get() = mMediaDuration

    val mediaTitle: String
        get() = mMediaTitleLiveData.value ?: ""

    var isTracking = false

    fun setMediaTitle(title: String) {
        mMediaTitleLiveData.value = title
    }

    fun setIsMediaPlaying(isMediaPlaying: Boolean) {
        mIsMediaPlaying.value = isMediaPlaying
    }

    fun setMediaProgress(progress: Long) {
        mMediaProgress.value = progress
    }

    fun setMediaDuration(duration: Long) {
        mMediaDuration.value = duration
    }

}