package com.xenderx.mediaplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.models.Video
import kotlinx.android.synthetic.main.cardview_audio.view.img_media_image
import kotlinx.android.synthetic.main.cardview_audio.view.txt_media_subtitles
import kotlinx.android.synthetic.main.cardview_audio.view.txt_media_title

class VideoListAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    private var mMediaList: ArrayList<Video> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.cardview_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.mTitleText.text = mMediaList[position].title
        holder.mSubtitleText.text = mMediaList[position].artist
        Glide.with(context)
            .load(mMediaList[position].uri)
            .placeholder(R.drawable.ic_ondemand_video_24dp)
            .into(holder.mVideoThumb)
    }

    fun setVideoList(mediaList: ArrayList<Video>) {
        mMediaList = mediaList
        notifyDataSetChanged()
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTitleText: TextView = itemView.txt_media_title
        val mSubtitleText: TextView = itemView.txt_media_subtitles
        val mVideoThumb: ImageView = itemView.img_media_image
    }

}