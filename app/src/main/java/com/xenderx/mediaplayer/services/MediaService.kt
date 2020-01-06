package com.xenderx.mediaplayer.services

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.xenderx.mediaplayer.MediaApplication
import com.xenderx.mediaplayer.utils.PreferenceUtil
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.notifications.MediaNotificationManager
import com.xenderx.mediaplayer.player.MediaPlayerAdapter
import com.xenderx.mediaplayer.player.PlaybackListener
import com.xenderx.mediaplayer.utils.Constents
import com.xenderx.mediaplayer.utils.Constents.MEDIA_QUEUE_INDEX


class MediaService : MediaBrowserServiceCompat() {

    companion object {
        const val TAG = "MediaService"
    }

    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var mPlayback: MediaPlayerAdapter
    private lateinit var mMediaApplication: MediaApplication
    private lateinit var mPreferenceUtil: PreferenceUtil
    private lateinit var mMediaNotificationManager: MediaNotificationManager
    private var mIsServiceRunning = false


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        mMediaApplication = MediaApplication.getInstance()
        mPreferenceUtil = PreferenceUtil(this)

        mMediaSession = MediaSessionCompat(this, TAG)
        mMediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
        )

        mMediaSession.setCallback(MediaSessionCallback())
        sessionToken = mMediaSession.sessionToken
        mPlayback = MediaPlayerAdapter(this, MediaPlayerListener())

        mMediaNotificationManager = MediaNotificationManager(this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mPlayback.stop()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaSession.release()
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (TextUtils.equals("empty_media", parentId)) {
            result.sendResult(null)
            return
        }
        result.sendResult(mMediaApplication.mediaItems as MutableList<MediaBrowserCompat.MediaItem>)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        if (clientPackageName == applicationContext.packageName) {
            // Allowed to browse media
            return BrowserRoot("playlist", null)
        }

        return BrowserRoot("empty_media", null)

    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        private val mPlaylist: MutableList<MediaSessionCompat.QueueItem> = ArrayList()
        private var mQueueIndex = -1
        private var mPreparedMedia: MediaMetadataCompat? = null

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            mPlaylist.add(
                MediaSessionCompat.QueueItem(
                    description,
                    description.hashCode().toLong()
                )
            )
            mQueueIndex = if (mQueueIndex == -1) 0 else mQueueIndex
            mMediaSession.setQueue(mPlaylist)
        }

        override fun onSkipToPrevious() {
            mQueueIndex = if (mQueueIndex > 0) mQueueIndex - 1 else mPlaylist.size - 1
            mPreparedMedia = null
            onPlay()
        }

        override fun onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) { // Nothing to play.
                return
            }
            val item = mPlaylist[mQueueIndex]
            val desc = item.description
            val mediaId = desc.mediaId
            mPreparedMedia = mMediaApplication.getMediaItem(mediaId)
            mMediaSession.setMetadata(mPreparedMedia)

            if (!mMediaSession.isActive) {
                mMediaSession.isActive = true
            }
        }

        override fun onPlay() {
            if (!isReadyToPlay()) {
                // Nothing to play.
                return
            }

            if (mPreparedMedia == null) {
                onPrepare()
            }
            val mediaId = mPlaylist[mQueueIndex].description.mediaId
            mMediaApplication.playingMediaId = mediaId ?: ""
            mPreferenceUtil.saveQueueIndex(mQueueIndex)
            mPreferenceUtil.saveLatPlayedMediaId(mediaId!!)

            mPlayback.playFromMedia(mPreparedMedia!!)
        }

        override fun onSkipToNext() {
            mQueueIndex = (++mQueueIndex % mPlaylist.size)
            mPreparedMedia = null
            onPlay()
        }

        override fun onPause() {
            mPlayback.pause()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
//            mPlaylist.forEach {
//                val id = it.description.mediaId
//                if (id == mediaId) {
//                    mQueueIndex = mPlaylist.indexOf(it)
//                    mPreparedMedia = null
//                    onPlay()
//                    return@forEach
//                }
//            }

            mQueueIndex = extras?.getInt(MEDIA_QUEUE_INDEX, -1) ?: -1
            if (mQueueIndex == -1) mQueueIndex++
            mPreparedMedia = null
            onPlay()
        }

        override fun onSeekTo(pos: Long) {
            mPlayback.seekTo(pos)
        }

        override fun onStop() {
            mPlayback.stop()
            mMediaSession.isActive = false
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            mPlaylist.remove(
                MediaSessionCompat.QueueItem(
                    description,
                    description.hashCode().toLong()
                )
            )
            mQueueIndex = if (mPlaylist.isEmpty()) -1 else mQueueIndex
            mMediaSession.setQueue(mPlaylist)
        }

        private fun isReadyToPlay(): Boolean {
            return mPlaylist.isNotEmpty()
        }
    }

    private inner class MediaPlayerListener : PlaybackListener {

        private val mServiceManager: ServiceManager = ServiceManager()

        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            mMediaSession.setPlaybackState(state)

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> mServiceManager.displayNotification(state)
                PlaybackStateCompat.STATE_PAUSED -> mServiceManager.displayNotification(state)
                PlaybackStateCompat.STATE_STOPPED -> mServiceManager.moveServiceOutOfStartedState()
            }
        }

        override fun seekTo(progress: Long, max: Long) {
            val intent = Intent().apply {
                putExtra(Constents.SEEK_BAR_PROGRESS, progress)
                putExtra(Constents.SEEK_BAR_MAX, max)
                action = getString(R.string.broadcast_seekbar_update)
            }

            sendBroadcast(intent)
        }

        override fun onPlaybackComplete() {
            mMediaSession.controller.transportControls.skipToNext()
        }

        inner class ServiceManager {
            private lateinit var mState: PlaybackStateCompat

            fun displayNotification(state: PlaybackStateCompat) {
                val notification: Notification

                when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        notification = mMediaNotificationManager.buildNotification(
                            state, sessionToken, mPlayback.currentMedia?.description, null
                        )

                        if (!mIsServiceRunning) {
                            ContextCompat.startForegroundService(
                                this@MediaService,
                                Intent(this@MediaService, MediaService::class.java)
                            )
                            mIsServiceRunning = true
                        }

                        startForeground(MediaNotificationManager.NOTIFICATION_ID, notification)
                    }

                    PlaybackStateCompat.STATE_PAUSED -> {
                        stopForeground(false)

                        notification = mMediaNotificationManager.buildNotification(
                            state, sessionToken, mPlayback.currentMedia?.description, null
                        )

                        mMediaNotificationManager.notificationManager.notify(
                            MediaNotificationManager.NOTIFICATION_ID, notification
                        )
                    }
                }
            }

            fun moveServiceOutOfStartedState() {
                stopForeground(true)
                stopSelf()
                mIsServiceRunning = false
            }
        }

    }
}