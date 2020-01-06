package com.xenderx.mediaplayer.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.xenderx.mediaplayer.R


class MediaPlayerAdapter(
    private val mContext: Context,
    private val mPlaybackListener: PlaybackListener
) : PlayerAdapter(mContext) {

    companion object {
        const val TAG = "MediaPlayerAdapter"
    }

    private var mExoPlayer: SimpleExoPlayer? = null
    private var mTrackSelector: TrackSelector? = null
    private var mRenderer: DefaultRenderersFactory? = null
    private var mDataSourceFactory: DataSource.Factory? = null
    private var mCurrentMediaPlayedToCompletion: Boolean = false
    private var mState: Int = PlaybackStateCompat.STATE_STOPPED
    private var mExoPlayerEventListener: ExoPlayerEventListener? = null

    override var currentMedia: MediaMetadataCompat? = null
    override val isPlaying: Boolean
        get() = mExoPlayer != null && mExoPlayer!!.playWhenReady

    private fun initializePlayer() {
        if (mExoPlayer == null) {
            mTrackSelector = DefaultTrackSelector()
            mRenderer = DefaultRenderersFactory(mContext)
            mDataSourceFactory = DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, mContext.getString(R.string.app_name))
            )

            mExoPlayer = ExoPlayerFactory.newSimpleInstance(mRenderer, mTrackSelector, DefaultLoadControl())
            if (mExoPlayerEventListener == null) {
                mExoPlayerEventListener = ExoPlayerEventListener()
            }
            mExoPlayer?.addListener(mExoPlayerEventListener)

        }
    }

    private fun releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer?.release()
            mExoPlayer = null
        }
    }



    override fun playFromMedia(metadata: MediaMetadataCompat) {
        startTrackingPlayback()
        playFile(metadata)
    }

    override fun seekTo(position: Long) {
        mExoPlayer?.let {
            it.seekTo(position)
            setNewState(mState)
        }
    }

    override fun setVolume(volume: Float) {
        mExoPlayer?.volume = volume
    }

    override fun onPlay() {
        mExoPlayer?.let { exoPlayer ->
            if (!exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = true
                setNewState(PlaybackStateCompat.STATE_PLAYING)
            }
        }
    }

    override fun onPause() {
        mExoPlayer?.let { exoPlayer ->
            if (exoPlayer.playWhenReady) {
                exoPlayer.playWhenReady = false
                setNewState(PlaybackStateCompat.STATE_PAUSED)
            }
        }
    }

    override fun onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        releasePlayer()
    }

    private fun startTrackingPlayback() {
        Handler().let { handler ->
            handler.postDelayed(object : Runnable {
                override fun run() {
                    mExoPlayer?.let {
                        if (isPlaying) {
                            mPlaybackListener.seekTo(it.currentPosition, it.duration)

                            handler.postDelayed(this, 100)
                        }

                        if (it.currentPosition >= it.duration &&
                                it.duration > 0) {
                            mPlaybackListener.onPlaybackComplete()
                        }
                    }
                }
            }, 100)
        }
    }

    private fun playFile(metaData: MediaMetadataCompat) {
        val mediaId = metaData.description.mediaId
        var mediaChanged =
            currentMedia == null || mediaId != currentMedia!!.description.mediaId
        if (mCurrentMediaPlayedToCompletion) { // Last audio file was played to completion, the resourceId hasn't changed, but the
// player was released, so force a reload of the media file for playback.
            mediaChanged = true
            mCurrentMediaPlayedToCompletion = false
        }
        if (!mediaChanged) {
            if (!isPlaying) {
                play()
            }
            return
        } else {
            releasePlayer()
        }
        currentMedia = metaData
        initializePlayer()
        try {
            val audioSource: MediaSource = ExtractorMediaSource.Factory(mDataSourceFactory)
                .createMediaSource(Uri.parse(metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)))
            mExoPlayer!!.prepare(audioSource)
            Log.d(TAG, "onPlayerStateChanged: PREPARE")
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to play media uri: "
                        + metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI), e
            )
        }
        play()
    }

    fun setNewState(@PlaybackStateCompat.State newPlayerState: Int) {
        mState = newPlayerState

        // Whether playback goes to completion, or whether it is stopped, the
        // mCurrentMediaPlayedToCompletion is set to true.

        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true
        }
        val reportPosition =
            if (mExoPlayer == null) 0 else mExoPlayer!!.currentPosition

        // Send playback state information to service
        publishStateBuilder(reportPosition)
    }

    private fun publishStateBuilder(reportPosition: Long) {
        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(getAvailableActions())
        stateBuilder.setState(
            mState,
            reportPosition,
            1.0f,
            SystemClock.elapsedRealtime()
        )
        mPlaybackListener.onPlaybackStateChange(stateBuilder.build())
//        mPlaybackListener.updateUI(mCurrentMedia.getDescription().getMediaId())
    }

    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long {
        var actions = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        when (mState) {
            PlaybackStateCompat.STATE_STOPPED -> actions =
                actions or (PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE)

            PlaybackStateCompat.STATE_PLAYING -> actions =
                actions or (PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SEEK_TO)

            PlaybackStateCompat.STATE_PAUSED -> actions =
                actions or (PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP)

            else -> actions =
                actions or (PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE)
        }
        return actions
    }


    private inner class ExoPlayerEventListener : Player.EventListener {
        override fun onTimelineChanged(
            timeline: Timeline,
            manifest: Any?,
            reason: Int
        ) {}
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {}
        override fun onLoadingChanged(isLoading: Boolean) {}

        override fun onPlayerStateChanged(
            playWhenReady: Boolean,
            playbackState: Int
        ) {
            when (playbackState) {
                Player.STATE_ENDED -> {
                    setNewState(PlaybackStateCompat.STATE_PAUSED)
                }
                Player.STATE_BUFFERING -> {
//                    Log.d(
//                        PlayerAdapter.TAG,
//                        "onPlayerStateChanged: BUFFERING"
//                    )
//                    mStartTime = System.currentTimeMillis()
                }
                Player.STATE_IDLE -> {
                }
                Player.STATE_READY -> {
//                    Log.d(PlayerAdapter.TAG, "onPlayerStateChanged: READY")
//                    Log.d(
//                        PlayerAdapter.TAG,
//                        "onPlayerStateChanged: TIME ELAPSED: " + (System.currentTimeMillis() - mStartTime)
//                    )
                }
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
        override fun onPlayerError(error: ExoPlaybackException) {
            Log.e(TAG, "${error.message}", error)
        }
        override fun onPositionDiscontinuity(reason: Int) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
        override fun onSeekProcessed() {}
    }

}