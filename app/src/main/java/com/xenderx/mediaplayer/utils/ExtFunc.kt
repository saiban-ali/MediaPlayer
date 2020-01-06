package com.xenderx.mediaplayer.utils

import android.support.v4.media.MediaMetadataCompat
import com.xenderx.mediaplayer.models.Media
import com.xenderx.mediaplayer.models.Song

fun Media.toMediaMetadata(): MediaMetadataCompat = if (this is Song) {
    MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.id.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.artist)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.uri.toString())
        .build()
} else {
    MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.id.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.artist)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.uri.toString())
        .build()
}

fun List<Media>.toMediaMetadataList(): List<MediaMetadataCompat> =
    ArrayList<MediaMetadataCompat>().apply {
        this@toMediaMetadataList.forEach {
            this.add(it.toMediaMetadata())
        }
    }