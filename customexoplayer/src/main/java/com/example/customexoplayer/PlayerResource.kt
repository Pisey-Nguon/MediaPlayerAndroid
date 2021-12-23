package com.example.customexoplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerResource(val mediaName:String, val mediaUrl:String,val subtitles:ArrayList<SubtitleModel> = ArrayList()):Parcelable
