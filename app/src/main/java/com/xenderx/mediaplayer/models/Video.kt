package com.xenderx.mediaplayer.models

import android.net.Uri
import com.xenderx.mediaplayer.models.Media

data class Video(
    override val id: Long,
    override val title: String,
    override val artist: String,
    override val uri: Uri
) : Media
