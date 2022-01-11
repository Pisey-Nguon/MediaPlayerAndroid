package com.example.customexoplayer.components.player.media

import android.graphics.drawable.Drawable
import androidx.lifecycle.Lifecycle
import com.google.android.exoplayer2.ExoPlayer
import com.example.customexoplayer.PlayerResource

interface PlayerController:AdController {

    fun setPlayerResource(playerResource: PlayerResource): PlayerController
    fun getPlayerResource(): PlayerResource?
    fun setLifecycle(lifecycle:Lifecycle): PlayerController
    fun setResizingEnabled(resizingEnabled:Boolean): PlayerController
    fun setDownloadEnabled(downloadEnabled:Boolean):PlayerController
    fun setFullScreenEnabled(fullScreenEnabled:Boolean): PlayerController
    fun setMoreOptionEnabled(moreOptionEnabled:Boolean): PlayerController
    fun setTimeBarEnabled(timeBarEnabled:Boolean): PlayerController
    fun setIconPlay(iconPlay: Drawable): PlayerController
    fun setIconPause(iconPause: Drawable): PlayerController
    fun setIconReplay(iconReplay: Drawable): PlayerController
    fun setColorBackgroundProgressIndicator(colorBackgroundProgressIndicator:Int): PlayerController
    fun addDownloadListener(downloadState: DownloadState): PlayerController
    fun buildOnline()
    fun buildOffline()
    fun buildResume(player:ExoPlayer): PlayerController
    fun getPlayer():ExoPlayer?
}