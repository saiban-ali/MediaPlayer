package com.xenderx.mediaplayer.ui.fragments

import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xenderx.mediaplayer.utils.PermissionsUtil
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.models.Song
import com.xenderx.mediaplayer.adapters.SongListAdapter
import com.xenderx.mediaplayer.utils.toMediaMetadataList
import com.xenderx.mediaplayer.utils.IFragmentCallback
import kotlinx.android.synthetic.main.cardview_audio.view.*
import kotlinx.android.synthetic.main.fragment_audio.view.*
import java.lang.ClassCastException


class AudioFragment : Fragment() {

    private lateinit var mSongListAdapter: SongListAdapter
    private lateinit var mFragmentCallback: IAudioFragmentCallback

    override fun onAttach(context: Context) {

        if (context is IAudioFragmentCallback) {
            mFragmentCallback = context
        } else {
            throw ClassCastException("MainActivity must implement IFragmentCallback")
        }

        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_audio, container, false)

        mSongListAdapter =
            SongListAdapter(activity!!)

        view.recycler_view_home.adapter = mSongListAdapter
        view.recycler_view_home.layoutManager = LinearLayoutManager(activity)
        view.recycler_view_home.addItemDecoration(
            DividerItemDecoration(
                view.recycler_view_home.context,
                DividerItemDecoration.HORIZONTAL
            )
        )

        mSongListAdapter.onSongClickListener = object: SongListAdapter.ISongClickListener {
            override fun onSongClick(position: Int, view: View) {
                mFragmentCallback.playSongWithId(position)
                mFragmentCallback.mediaViewModel.setMediaTitle(view.txt_media_title.text.toString())
            }
        }

        getSongs()

        return view
    }

    private fun getSongs() {
        PermissionsUtil.checkStoragePermissions(activity!!)
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3"))
        val cursor = context!!.contentResolver.query(uri, null, selection, selectionArgs, null)
//        val videoCursor = context!!.contentResolver.query(videoUri, null, null, null, null)

        if (cursor != null) {

            val songList = ArrayList<Song>()

            while (cursor.moveToNext()) {
                val title = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                )
                val artist = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                )
                val id = cursor.getLong(
                    cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                )
                val contentUri =
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )



                val song = Song(id, title, artist, contentUri)
                songList.add(song)

//                MediaLibrary.addMedia(song)
            }
            mFragmentCallback.mediaApplication.setMediaItems(songList.toMediaMetadataList())
            mFragmentCallback.subscribePlaylist()
            mSongListAdapter.setSongList(songList)
            mFragmentCallback.mediaViewModel.setMediaTitle(
                run {
                    val index = mFragmentCallback.preferenceUtil.getQueueIndex()
                    if (index != -1 && index < songList.size) {
                        songList[index].title
                    } else {
                        songList[0].title
                    }
                }
            )
            cursor.close()
        }
    }



    interface IAudioFragmentCallback : IFragmentCallback {

        fun subscribePlaylist()
        fun playSongWithId(index: Int)

    }
}
