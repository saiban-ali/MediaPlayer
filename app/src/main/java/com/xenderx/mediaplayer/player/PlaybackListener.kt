package com.xenderx.mediaplayer.player

import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackListener {

    fun onPlaybackStateChange(state: PlaybackStateCompat)

    fun seekTo(progress: Long, max: Long)

    fun onPlaybackComplete()

}