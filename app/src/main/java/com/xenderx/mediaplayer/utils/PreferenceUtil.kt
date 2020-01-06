package com.xenderx.mediaplayer.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.xenderx.mediaplayer.utils.Constents.LAST_PLAYED_MEDIA_ID
import com.xenderx.mediaplayer.utils.Constents.MEDIA_QUEUE_INDEX

class PreferenceUtil(val context: Context) {

    private val mPreference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun saveQueueIndex(index: Int) {
        val editor = mPreference.edit()
        editor.putInt(MEDIA_QUEUE_INDEX, index)
        editor.apply()
    }

    fun getQueueIndex(): Int = mPreference.getInt(MEDIA_QUEUE_INDEX, -1)

    fun saveLatPlayedMediaId(mediaId: String) {
        val editor = mPreference.edit()
        editor.putString(LAST_PLAYED_MEDIA_ID, mediaId)
        editor.apply()
    }

    fun getLastPlayedMediaId(): String = mPreference.getString(LAST_PLAYED_MEDIA_ID, "")!!

}