package com.example.customexoplayer.components.player.media

import com.example.customexoplayer.PlayerResource

interface DownloadState {
    fun onDownloadCompleted(playerResource: PlayerResource)
    fun onDownloadStarted(playerResource: PlayerResource)
    fun onDownloadFailed(playerResource: PlayerResource)
    fun onVideoHasBeenDownloaded(playerResource: PlayerResource)
}