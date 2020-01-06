package com.xenderx.mediaplayer.utils

import android.content.ComponentName
import android.content.Context
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat


class MediaBrowserHelper(
    private val mContext: Context,
    private val mMediaBrowserServiceClass: Class<out MediaBrowserServiceCompat>
) {

    private val TAG = "MediaBrowserHelper"

    private var mConfigurationChanged = false
    private var mMediaBrowser: MediaBrowserCompat? = null
    private var mMediaController: MediaControllerCompat? = null
    private var mMediaBrowserConnectionCallback: MediaBrowserConnectionCallback
    private var mMediaBrowserSubscriptionCallback: MediaBrowserSubscriptionCallback
    private var mMediaControllerCallback: MediaControllerCallback

    var mMediaBrowserCallback: Callback? = null


    init {
        mMediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
        mMediaBrowserSubscriptionCallback = MediaBrowserSubscriptionCallback()
        mMediaControllerCallback = MediaControllerCallback()
    }

    fun onStart(configurationChanged: Boolean) {
        mConfigurationChanged = configurationChanged
        if (mMediaBrowser == null) {
            mMediaBrowser = MediaBrowserCompat(
                mContext,
                ComponentName(mContext, mMediaBrowserServiceClass),
                mMediaBrowserConnectionCallback,
                null
            ).apply {
                connect()
            }
        }
    }

    fun onStop() {
        mMediaController?.let {
            it.unregisterCallback(mMediaControllerCallback)
            mMediaController = null
        }

        mMediaBrowser?.let {
            if (it.isConnected) {
                it.disconnect()
                mMediaBrowser = null
            }
        }
    }

    fun subscribePlaylist(playlistId: String) {
        mMediaBrowser?.subscribe(playlistId, mMediaBrowserSubscriptionCallback)
    }

    fun getTransportControls(): MediaControllerCompat.TransportControls? =
        mMediaController?.transportControls
            ?: throw IllegalStateException("MediaController is null!")


    private inner class MediaBrowserConnectionCallback :
        MediaBrowserCompat.ConnectionCallback() {
        // Happens as a result of onStart().
        override fun onConnected() {
            mMediaBrowser?.let {

                try { // Get a MediaController for the MediaSession.
                    mMediaController = MediaControllerCompat(mContext, it.sessionToken)
                    mMediaController?.registerCallback(mMediaControllerCallback)
                    mMediaBrowserCallback?.onMediaControllerConnected(mMediaController!!)
                } catch (e: RemoteException) {
                    throw RuntimeException(e)
                }

                it.subscribe(it.root, mMediaBrowserSubscriptionCallback)
                mMediaBrowserCallback?.onMediaControllerConnected(mMediaController!!)
            }
        }
    }


    inner class MediaBrowserSubscriptionCallback :
        MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaBrowserCompat.MediaItem>
        ) {
            if (!mConfigurationChanged) {
                for (mediaItem in children) {
                    mMediaController?.addQueueItem(mediaItem.description)
                }
            }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { mMediaBrowserCallback?.onPlaybackStateChanged(it) }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let { mMediaBrowserCallback?.onMetadataChanged(it) }
        }
    }

    interface Callback {
        fun onPlaybackStateChanged(state: PlaybackStateCompat)
        fun onMetadataChanged(metadata: MediaMetadataCompat)
        fun onMediaControllerConnected(mediaControllerCompat: MediaControllerCompat)
    }

}