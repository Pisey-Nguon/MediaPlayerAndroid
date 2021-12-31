package com.example.customexoplayer.components.player.custom.skipad

import com.example.customexoplayer.components.player.media.model.AdResource
import com.google.android.exoplayer2.ExoPlayer

interface SkipController {
    fun setAdResource(adResource: AdResource?):SkipController
    fun setCountDown(duration:String)
    fun addButtonSkipListener(onSkipped:() -> Unit):SkipController
    fun pauseCountDown()
    fun resumeCountDown()
    fun build()
}