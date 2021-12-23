package com.example.customexoplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubtitleModel(var subtitleUrl:String, val language: String):Parcelable