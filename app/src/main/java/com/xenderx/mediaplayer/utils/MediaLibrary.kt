package com.xenderx.mediaplayer.utils

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.xenderx.mediaplayer.models.Media
import com.xenderx.mediaplayer.models.Song
import com.xenderx.mediaplayer.models.Video
import java.util.*
import kotlin.collections.ArrayList


class MediaLibrary {

    var mMediaList: MutableList<MediaMetadataCompat> = ArrayList()

    private var mMediaMap: TreeMap<String, MediaMetadataCompat> = TreeMap()
    val treeMap: TreeMap<String, MediaMetadataCompat>
        get() = mMediaMap

    init {
        initMap()
    }

    private fun initMap() {
        for (media in mediaLibrary) {
            val mediaId = media.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            mMediaMap[mediaId] = media
            mMediaList.add(media)
        }
    }

    companion object {
        private const val TAG = "MediaLibrary"

        val mediaLibrary = ArrayList<MediaMetadataCompat>()

        fun getPlaylistMedia(mediaIds: Set<String>): List<MediaBrowserCompat.MediaItem> {
            val result: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
            // VERY INEFFICIENT WAY TO DO THIS (BUT I NEED TO BECAUSE THE DATA STRUCTURE ARE NOT IDEAL)
// RETRIEVING DATA FROM A SERVER WOULD NOT POSE THIS ISSUE
            for (id in mediaIds) {
                for (metadata in mediaLibrary) {
                    if (id == metadata.description.mediaId) {
                        result.add(
                            MediaBrowserCompat.MediaItem(
                                metadata.description,
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                            )
                        )
                    }
                }
            }
            return result
        }

        val mediaItems: List<MediaBrowserCompat.MediaItem>
            get() {
                val result: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
                for (metadata in mediaLibrary) {
                    result.add(
                        MediaBrowserCompat.MediaItem(
                            metadata.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                        )
                    )
                }
                return result
            }


        fun <T: Media> addMedia(media: T) {
            val mediaMetadataCompat: MediaMetadataCompat = if (media is Song) {
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.id.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, media.uri.toString())
                    .build()
            } else {
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.id.toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, media.uri.toString())
                    .build()
            }

            mediaLibrary.add(mediaMetadataCompat)
        }

            /*arrayOf(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11111")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Jim Wilson")
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                    "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    "CodingWithMitch Podcast #1 - Jim Wilson"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    "http://content.blubrry.com/codingwithmitch/Interview_audio_online-audio-converter.com_.mp3"
                )
                .build(),
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11112")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Justin Mitchel")
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                    "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    "CodingWithMitch Podcast #2 - Justin Mitchel"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    "http://content.blubrry.com/codingwithmitch/Justin_Mitchel_interview_audio_online-audio-converter.com_.mp3"
                )
                .build(),
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11113")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian & Matt Tran")
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                    "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    "CodingWithMitch Podcast #3 - Matt Tran"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    "http://content.blubrry.com/codingwithmitch/Matt_Tran_Interview_online-audio-converter.com_.mp3"
                )
                .build(),
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "11114")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Mitch Tabian")
                .putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                    "https://codingwithmitch.s3.amazonaws.com/static/profile_images/default_avatar.jpg"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    "Some Random Test Audio"
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    "https://s3.amazonaws.com/codingwithmitch-static-and-media/pluralsight/Processes+and+Threads/audio+test+1+(online-audio-converter.com).mp3"
                )
                .build()
        )*/

    }
}
