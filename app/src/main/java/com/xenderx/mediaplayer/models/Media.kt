package com.xenderx.mediaplayer.models

import android.net.Uri

interface Media {
    val id: Long
    val title: String
    val artist: String
    val uri: Uri
}