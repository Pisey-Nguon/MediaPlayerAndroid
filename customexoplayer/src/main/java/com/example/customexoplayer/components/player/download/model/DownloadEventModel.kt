package com.example.customexoplayer.components.player.download.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadEventModel(val state:Int, val mediaUrl:String):Parcelable
