package com.xenderx.mediaplayer.ui.fragments


import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.xenderx.mediaplayer.utils.PermissionsUtil
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.models.Video
import com.xenderx.mediaplayer.adapters.VideoListAdapter
import com.xenderx.mediaplayer.utils.MediaLibrary
import kotlinx.android.synthetic.main.fragment_video.view.*

class VideoFragment : Fragment() {

    private lateinit var videoListAdapter: VideoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        videoListAdapter =
            VideoListAdapter(activity!!)

        view.recycler_view_video.adapter = videoListAdapter
        view.recycler_view_video.layoutManager = GridLayoutManager(activity, 2)

        getVideos()

        return view
    }

    private fun getVideos() {
        PermissionsUtil.checkStoragePermissions(activity!!)
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = context!!.contentResolver.query(uri, null, null, null, null)
//        val videoCursor = context!!.contentResolver.query(videoUri, null, null, null, null)

        if (cursor != null) {

            val videoList = ArrayList<Video>()

            while (cursor.moveToNext()) {

                val title = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                )

                val artist = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)
                )

                val id = cursor.getLong(
                    cursor.getColumnIndex(MediaStore.Video.Media._ID)
                )
                val contentUri =
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )


                val video = Video(id, title, artist, contentUri)
                videoList.add(video)
//                MediaLibrary.addMedia(video)
            }

            /*while (videoCursor.moveToNext()) {
                val song = Song(videoCursor.getString(
                    videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                ))
                songList.add(song)
            }*/

            videoListAdapter.setVideoList(videoList)

            cursor.close()
        }
    }

}
