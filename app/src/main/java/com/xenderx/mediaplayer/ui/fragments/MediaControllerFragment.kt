package com.xenderx.mediaplayer.ui.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.xenderx.mediaplayer.R
import com.xenderx.mediaplayer.utils.IFragmentCallback
import kotlinx.android.synthetic.main.fragment_media_controller.*
import kotlinx.android.synthetic.main.fragment_media_controller.view.*
import java.lang.ClassCastException

class MediaControllerFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var fragmentCallback: IMediaFragmentCallback
    private lateinit var mView: View

    override fun onAttach(context: Context) {
        if (context is IMediaFragmentCallback) {
            fragmentCallback = context
        } else {
            throw ClassCastException("MainActivity must implement IMediaFragmentCallback")
        }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_media_controller, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        seek_bar.setOnSeekBarChangeListener(this)

        setupClickListeners(view)

        setupObservers()
    }

    private fun setupClickListeners(view: View) {
        view.img_btn_play_pause.setOnClickListener {
            fragmentCallback.onTogglePlayPause()
        }

        view.img_btn_skip_next.setOnClickListener {
            fragmentCallback.onSkipNext()
        }

        view.img_btn_skip_previous.setOnClickListener {
            fragmentCallback.onSkipPrevious()
        }
    }

    private fun setupObservers() {
        fragmentCallback.mediaViewModel
            .mediaTitleLiveData
            .observe(viewLifecycleOwner, Observer { title ->
                setMediaTitle(title)
            })

        fragmentCallback.mediaViewModel
            .isMediaPlaying
            .observe(viewLifecycleOwner, Observer {
                setIsMediaPlaying(it)
            })

        fragmentCallback.mediaViewModel
            .mediaProgress
            .observe(viewLifecycleOwner, Observer {
                seek_bar.progress = it.toInt()
            })

        fragmentCallback.mediaViewModel
            .mediaDuration
            .observe(viewLifecycleOwner, Observer {
                seek_bar.max = it.toInt()
            })
    }

    private fun setMediaTitle(title: String) {
        mView.txt_media_title.text = title
    }

    private fun setIsMediaPlaying(isPlaying: Boolean) {
        img_btn_play_pause.setImageResource(
            if (isPlaying) R.drawable.ic_pause_24dp
            else R.drawable.ic_play_arrow_24dp
        )
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        fragmentCallback.mediaViewModel.isTracking = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        val progress = seekBar?.progress?.toLong() ?: 0
        fragmentCallback.mediaViewModel.isTracking = false
        fragmentCallback.mediaApplication.mediaController?.transportControls?.seekTo(progress)
    }

    interface IMediaFragmentCallback : IFragmentCallback {

        fun onTogglePlayPause()
        fun onSkipNext()
        fun onSkipPrevious()
    }
}
