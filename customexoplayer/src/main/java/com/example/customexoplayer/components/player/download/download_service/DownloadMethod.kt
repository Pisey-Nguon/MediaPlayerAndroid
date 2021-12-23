package com.example.customexoplayer.components.player.download.download_service

import android.content.Context
import android.content.Intent
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.DownloadService
import com.example.customexoplayer.PlayerResource
import com.example.customexoplayer.components.utils.Constants
import com.example.customexoplayer.components.utils.PlayerUtil

class DownloadMethod(private val context: Context) {

    fun startDownload(playerResource: PlayerResource?, trackIndexMovie:Int) {
        val intent = Intent(context, PrepareDownloadService::class.java)
        intent.putExtra(Constants.EXO_PLAYER_RESOURCE,playerResource)
        intent.putExtra(Constants.EXO_TRACK_INDEX,trackIndexMovie)
        context.startService(intent)
    }

    fun cancelDownload() {
        DownloadService.sendRemoveAllDownloads(context, DownloadService::class.java, false)
    }

    fun isDownloaded(url: String): Boolean {
        val mediaItem = MediaItem.fromUri(url)
        return PlayerUtil.getDownloadTracker(context).isDownloaded(mediaItem)
    }


}