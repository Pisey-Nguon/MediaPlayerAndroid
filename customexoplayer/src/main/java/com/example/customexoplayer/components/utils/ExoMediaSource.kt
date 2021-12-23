package com.example.customexoplayer.components.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.example.customexoplayer.PlayerResource


class ExoMediaSource(private val context:Context): MediaSourceBuilderControl {

    private var playerResource: PlayerResource? = null

    private fun buildVideoMediaSource(uri: Uri, dataSourceFactory: DataSource.Factory): MediaSource {
        return when (@C.ContentType val type = Util.inferContentType(uri, null)) {
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(
                MediaItem.fromUri(uri))
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(uri))
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }
    private fun buildSubtitleMediaSource(uri: Uri, language:String?, dataSourceFactory: DataSource.Factory): MediaSource {
        val factory = SingleSampleMediaSource.Factory(dataSourceFactory)
        val subtitleFormat = MediaItem.SubtitleConfiguration.Builder(uri).setMimeType(MimeTypes.APPLICATION_SUBRIP).setLanguage(language).setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()
        return factory.createMediaSource( subtitleFormat, C.TIME_UNSET)
    }

    override fun setPlayerResource(playerResource: PlayerResource?): MediaSourceBuilderControl {
        this.playerResource = playerResource
        return this
    }

    override fun buildOnline(): MergingMediaSource {
        val size:Int = if (playerResource?.subtitles != null){
            playerResource?.subtitles?.size!!.plus(1)
        }else{
            1
        }
        val dataSourceFactory = PlayerUtil.getDataSourceFactory(context)
        val videoSource = buildVideoMediaSource(Uri.parse(playerResource?.mediaUrl),dataSourceFactory)

        val mediaSources : Array<MediaSource> = Array(size){
            if (it == 0){
                videoSource
            }else{
                buildSubtitleMediaSource(Uri.parse(playerResource?.subtitles?.get(it - 1)?.subtitleUrl),playerResource?.subtitles?.get(it - 1)?.language,dataSourceFactory)
            }
        }

        return MergingMediaSource(*mediaSources)
    }


    override fun buildOffline(): MergingMediaSource? {
        var mergingMediaSource:MergingMediaSource? = null

        val size:Int = if (playerResource?.subtitles != null){
            playerResource?.subtitles?.size!!.plus(1)
        }else{
            1
        }
        val dataSourceFactory = PlayerUtil.getDataSourceFactory(context)
        val downloadRequest = PlayerUtil.getDownloadTracker(context).getDownloadRequest(Uri.parse(playerResource?.mediaUrl))
        if (downloadRequest != null){
            playerResource = Gson().fromJson(Util.fromUtf8Bytes(downloadRequest.data), PlayerResource::class.java)
            val videoSource = DownloadHelper.createMediaSource(downloadRequest,dataSourceFactory)

            val mediaSources : Array<MediaSource> = Array(size){
                if (it == 0){
                    videoSource
                }else{
                    buildSubtitleMediaSource(Uri.parse(playerResource?.subtitles?.get(it - 1)?.subtitleUrl),playerResource?.subtitles?.get(it - 1)?.language,dataSourceFactory)
                }
            }
            mergingMediaSource = MergingMediaSource(*mediaSources)
        }else{
            mergingMediaSource = null
        }

       return mergingMediaSource

    }

}

interface MediaSourceBuilderControl{
    fun setPlayerResource(playerResource: PlayerResource?): MediaSourceBuilderControl
    fun buildOnline(): MergingMediaSource
    fun buildOffline():MergingMediaSource?
}

