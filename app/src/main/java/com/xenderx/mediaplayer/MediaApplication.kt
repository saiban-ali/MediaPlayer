package com.xenderx.mediaplayer

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class MediaApplication : Application() {

    companion object {
        const val TAG = "MediaApplication"
        private var mInstance: MediaApplication? = null
        fun getInstance(): MediaApplication {
            if (mInstance == null) {
                mInstance = MediaApplication()
            }
            return mInstance!!
        }

    }

    private val mMediaItems: ArrayList<MediaBrowserCompat.MediaItem> = ArrayList()
    private val mTreeMap: TreeMap<String, MediaMetadataCompat> = TreeMap()
    var mediaController: MediaControllerCompat? = null

    fun setMediaItems(mediaItems: List<MediaMetadataCompat>) {
        mMediaItems.clear()

        mediaItems.forEach { item ->
            mMediaItems.add(
                MediaBrowserCompat.MediaItem(
                    item.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            )

            item.description.mediaId?.let { id ->
                mTreeMap[id] = item
            }
        }
    }

    val mediaItems: List<MediaBrowserCompat.MediaItem>
        get() = mMediaItems

    val treeMap: TreeMap<String, MediaMetadataCompat>
        get() = mTreeMap

    lateinit var playingMediaId: String

    fun getMediaItem(mediaId: String?): MediaMetadataCompat =
        mTreeMap[mediaId] ?: throw IllegalArgumentException("MediaId does not exist")

    fun getPlayingMedia(): MediaMetadataCompat = mTreeMap[playingMediaId] ?: throw IllegalAccessError("MediaId not valid")

    fun disconnectMediaController() {
        mediaController = null
    }

}