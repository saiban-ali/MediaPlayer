package com.xenderx.mediaplayer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.xenderx.mediaplayer.*
import com.xenderx.mediaplayer.adapters.TabPagerAdapter
import com.xenderx.mediaplayer.services.MediaService
import com.xenderx.mediaplayer.ui.fragments.AudioFragment
import com.xenderx.mediaplayer.ui.fragments.MediaControllerFragment
import com.xenderx.mediaplayer.utils.Constents
import com.xenderx.mediaplayer.utils.Constents.MEDIA_QUEUE_INDEX
import com.xenderx.mediaplayer.utils.MediaBrowserHelper
import com.xenderx.mediaplayer.utils.PreferenceUtil
import com.xenderx.mediaplayer.viewmodels.MediaViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    AppCompatActivity(),
    MediaControllerFragment.IMediaFragmentCallback,
    AudioFragment.IAudioFragmentCallback,
    MediaBrowserHelper.Callback
{

    private var mAppOpen: Boolean = false
    private lateinit var mMediaApplication: MediaApplication
    override val mediaApplication: MediaApplication
        get() = mMediaApplication

    private lateinit var mMediaBrowserHelper: MediaBrowserHelper
    private lateinit var mPreferenceUtil: PreferenceUtil
    override val preferenceUtil: PreferenceUtil
        get() = mPreferenceUtil

    private lateinit var mMediaViewModel: MediaViewModel
    override val mediaViewModel: MediaViewModel
        get() = mMediaViewModel

    private var mSeekbarReceiver: SeekbarBroadcastReceiver? = null

    private var mIsPlaying = false
    private var mConfigurationChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "MediaPlayer"
        supportActionBar?.elevation = 0f

        mPreferenceUtil = PreferenceUtil(this)
        mMediaViewModel = ViewModelProviders.of(this).get(MediaViewModel::class.java)
        mMediaApplication = MediaApplication.getInstance()
        mMediaBrowserHelper = MediaBrowserHelper(
            this,
            MediaService::class.java
        )
        mMediaBrowserHelper.mMediaBrowserCallback = this

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.container, HomeFragment())
//            .commit()

        val tabAdapter =
            TabPagerAdapter(
                supportFragmentManager
            )
        container.adapter = tabAdapter
        tab_layout.setupWithViewPager(container)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mConfigurationChanged = true
    }

    override fun onStart() {
        super.onStart()
        mMediaBrowserHelper.onStart(mConfigurationChanged)
    }

    override fun onStop() {
        super.onStop()
        mAppOpen = false
        mMediaApplication.disconnectMediaController()
        mMediaBrowserHelper.onStop()
    }

    override fun onResume() {
        super.onResume()
        initSeekbarReceiver()
    }

    private fun initSeekbarReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(getString(R.string.broadcast_seekbar_update))
        }
        mSeekbarReceiver = SeekbarBroadcastReceiver()
        registerReceiver(mSeekbarReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()

        mSeekbarReceiver?.let {
            unregisterReceiver(it)
        }
    }

    override fun onTogglePlayPause() {
        if (mAppOpen) {
            mIsPlaying = if (mIsPlaying) {
                mMediaBrowserHelper.getTransportControls()?.pause()
                false
            } else {
                mMediaBrowserHelper.getTransportControls()?.play()
                true
            }
        } else {
            if (mPreferenceUtil.getLastPlayedMediaId() != "") {
                playSongWithId(mPreferenceUtil.getQueueIndex())
            } else {
//                Toast.makeText(this, "select media to play", Toast.LENGTH_SHORT).show()
                playSongWithId(0)
            }
        }
    }

    override fun onSkipNext() {
        mMediaBrowserHelper.getTransportControls()?.skipToNext()
    }

    override fun onSkipPrevious() {
        mMediaBrowserHelper.getTransportControls()?.skipToPrevious()
    }


    override fun subscribePlaylist() {
        mMediaBrowserHelper.subscribePlaylist("playlist")
    }

    override fun playSongWithId(index: Int) {
        if (mMediaApplication.mediaItems.isNotEmpty()) {
            mMediaBrowserHelper.getTransportControls()?.playFromMediaId(
                mMediaApplication.mediaItems[index].description.mediaId,
                Bundle().apply { putInt(MEDIA_QUEUE_INDEX, index) }
            )
//        setMediaControllerTitle(index)

            mIsPlaying = true
            mAppOpen = true
        }
    }

    private fun setMediaControllerTitle(title: String) {
        mMediaViewModel.setMediaTitle(title)
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        mIsPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        setMediaPlaying(mIsPlaying)
    }

    private fun setMediaPlaying(isPlaying: Boolean) {
        mMediaViewModel.setIsMediaPlaying(isPlaying)
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat) {
        setMediaControllerTitle(metadata.description.title.toString())
    }

    override fun onMediaControllerConnected(mediaControllerCompat: MediaControllerCompat) {
        mMediaApplication.mediaController = mediaControllerCompat
    }

    private inner class SeekbarBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getLongExtra(Constents.SEEK_BAR_PROGRESS, 0) ?: 0
            val max = intent?.getLongExtra(Constents.SEEK_BAR_MAX, 0) ?: 0

            if (!mMediaViewModel.isTracking) {
                mMediaViewModel.apply {
                    setMediaProgress(progress)
                    setMediaDuration(max)
                }
            }
        }

    }
}
