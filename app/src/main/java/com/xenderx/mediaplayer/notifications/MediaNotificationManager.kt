package com.xenderx.mediaplayer.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.services.MediaService
import com.xenderx.mediaplayer.ui.MainActivity

class MediaNotificationManager(mediaService: MediaService) {

    private val mMediaService: MediaService = mediaService
    private val mNotificationManager: NotificationManager
    val notificationManager: NotificationManager
        get() = mNotificationManager

    private lateinit var mPlayAction: NotificationCompat.Action
    private lateinit var mPauseAction: NotificationCompat.Action
    private lateinit var mNextAction: NotificationCompat.Action
    private lateinit var mPreviousAction: NotificationCompat.Action

    init {
        mNotificationManager =
            mMediaService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        setupActionButtons()

        mNotificationManager.cancelAll()
    }

    companion object {

        private const val CHANNEL_ID = "com.xenderx.mediaplayer.channel_0"
        private const val OPEN_UI_INTENT_REQUEST_CODE = 10001
        const val NOTIFICATION_ID: Int = 20001
        const val TAG = "NotificationManager"
    }

    private fun setupActionButtons() {
        mPlayAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow_24dp,
            mMediaService.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                mMediaService, PlaybackStateCompat.ACTION_PLAY
            )
        )

        mPauseAction = NotificationCompat.Action(
            R.drawable.ic_pause_24dp,
            mMediaService.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                mMediaService, PlaybackStateCompat.ACTION_PAUSE
            )
        )

        mNextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next_24dp,
            mMediaService.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                mMediaService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )

        mPreviousAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous_24dp,
            mMediaService.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                mMediaService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val name = "MediaSession"
            val description = "MediaSession for MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                this.enableLights(true)
                this.lightColor = Color.RED
                this.enableVibration(true)
                this.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
            }
            mNotificationManager.createNotificationChannel(channel)
        } else {
            Log.d(TAG, "channel already exists")
        }
    }

    private fun isOreoOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun buildNotification(
        @NonNull state: PlaybackStateCompat,
        token: MediaSessionCompat.Token?,
        description: MediaDescriptionCompat?,
        bitmap: Bitmap?
    ): Notification {

        if (isOreoOrHigher()) {
            createChannel()
        }

        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(mMediaService, CHANNEL_ID)

        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2)
        )
            .setColor(ContextCompat.getColor(mMediaService, R.color.notification_bg))
            .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
            .setContentIntent(createContentIntent())
            .setContentTitle(description!!.title)
            .setContentText(description.subtitle)
            .setLargeIcon(bitmap)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    mMediaService, PlaybackStateCompat.ACTION_STOP
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
            builder.addAction(mPreviousAction)
        }

        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)

        if (state.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
            builder.addAction(mNextAction)
        }

        return builder.build()
    }

    private fun createContentIntent(): PendingIntent {
        val openUiIntent = Intent(mMediaService, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            mMediaService,
            OPEN_UI_INTENT_REQUEST_CODE,
            openUiIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

}