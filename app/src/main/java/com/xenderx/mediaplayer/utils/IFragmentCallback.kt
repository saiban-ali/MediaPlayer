package com.xenderx.mediaplayer.utils

import com.xenderx.mediaplayer.MediaApplication
import com.xenderx.mediaplayer.viewmodels.MediaViewModel

interface IFragmentCallback {

    val mediaViewModel: MediaViewModel
    val mediaApplication: MediaApplication
    val preferenceUtil: PreferenceUtil

}