package com.xenderx.mediaplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.models.Song
import kotlinx.android.synthetic.main.cardview_audio.view.*

class SongListAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    private var mMediaList: ArrayList<Song> = ArrayList()
    val songList: List<Song>
    get() = mMediaList

    private var mSongClickListener: ISongClickListener? = null
    var onSongClickListener: ISongClickListener?
        get() = mSongClickListener
        set(value) {
            mSongClickListener = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.cardview_audio,
                parent,
                false
            ),
            mSongClickListener
        )
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.mTitleText.text = mMediaList[position].title
        holder.mSubtitleText.text = mMediaList[position].artist

    }

    fun setSongList(mediaList: ArrayList<Song>) {
        mMediaList = mediaList
        notifyDataSetChanged()
    }

    fun getSong(position: Int) = mMediaList[position]


    class SongViewHolder(itemView: View, listener: ISongClickListener?) : RecyclerView.ViewHolder(itemView) {
        var mTitleText: TextView = itemView.txt_media_title
        val mSubtitleText: TextView = itemView.txt_media_subtitles

        init {
            listener?.let {
                setupClickListeners(it)
            }
        }

        private fun setupClickListeners(listener: ISongClickListener) {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSongClick(adapterPosition, this.itemView)
                }
            }
        }
    }

    interface ISongClickListener {
        fun onSongClick(position: Int, view: View)
    }

}