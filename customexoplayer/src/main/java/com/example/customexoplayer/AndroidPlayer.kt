package com.example.customexoplayer

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.video.VideoSize
import com.example.customexoplayer.components.player.download.model.DownloadEventModel
import com.example.customexoplayer.components.player.media.ActionPlayerControl
import com.example.customexoplayer.components.player.media.DownloadState
import com.example.customexoplayer.components.player.media.LayoutControl
import com.example.customexoplayer.components.player.media.PlayerController
import com.example.customexoplayer.components.utils.BroadcastSender
import com.example.customexoplayer.components.utils.ConstantDownload
import com.example.customexoplayer.components.utils.ExoMediaSource
import com.example.customexoplayer.components.utils.PlayerUtil
import java.io.IOException


class AndroidPlayer: FrameLayout, PlayerController, ActionPlayerControl, DefaultLifecycleObserver {


    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var trackSelectionParameters: DefaultTrackSelector.Parameters
    private lateinit var layoutControl: LayoutControl
    private var player:ExoPlayer? = null
    private var downloadState: DownloadState? = null
    private var playerResource: PlayerResource? = null

    constructor(context: Context):super(context){
        init(null)
    }

    constructor(context: Context,attrs:AttributeSet):super(context,attrs){
        init(attrs = attrs)
    }

    constructor(context: Context,attrs: AttributeSet,defStyleAttr: Int):super(context,attrs,defStyleAttr){
        init(attrs = attrs)
    }

    private val analyticsListener = object : AnalyticsListener {
        override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime, videoSize: VideoSize) {
            super.onVideoSizeChanged(eventTime, videoSize)
            layoutControl.onWrapHeight(videoSize = videoSize)
        }

        override fun onLoadError(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) {
            super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
            layoutControl.showLoading()
        }

        override fun onPlayerError(
            eventTime: AnalyticsListener.EventTime,
            error: PlaybackException
        ) {
            super.onPlayerError(eventTime, error)
            layoutControl.showLoading()
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            super.onPlaybackStateChanged(eventTime, state)
            when(state){
                Player.STATE_IDLE ->{
                    layoutControl.hideLoading()
                }
                Player.STATE_BUFFERING -> {
                    layoutControl.showLoading()
                }
                Player.STATE_READY ->{
                    layoutControl.hideLoading()
                    layoutControl.onReadyToPlay()
                }
                Player.STATE_ENDED ->{
                    layoutControl.hideLoading()
                    layoutControl.onEndPlay()
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutControl.onSizeChanged(width = w,height = h)
    }

    private fun init(attrs: AttributeSet?){
        dataSourceFactory = PlayerUtil.getDataSourceFactory(context)
        trackSelectionParameters = ParametersBuilder(context).build()
        trackSelector = DefaultTrackSelector(context)
        layoutControl = LayoutControl(context,this,attrs)
    }

    override fun setPlayerResource(playerResource: PlayerResource): PlayerController {
        this.playerResource = playerResource
        return this
    }

    override fun getPlayerResource(): PlayerResource? {
        return playerResource
    }

    override fun setLifecycle(lifecycle: Lifecycle): PlayerController {
        lifecycle.addObserver(this)
        return this
    }

    override fun setResizingEnabled(resizingEnabled: Boolean): PlayerController {
        layoutControl.setResizingEnabled(resizingEnabled = resizingEnabled)
        return this
    }

    override fun setIconPlay(iconPlay: Drawable): PlayerController {
        layoutControl.setIconPlay(iconPlay = iconPlay)
        return this
    }

    override fun setIconPause(iconPause: Drawable): PlayerController {
        layoutControl.setIconPause(iconPause = iconPause)
        return this
    }

    override fun setIconReplay(iconReplay: Drawable): PlayerController {
        layoutControl.setIconReplay(iconReplay = iconReplay)
        return this
    }

    override fun setColorBackgroundProgressIndicator(colorBackgroundProgressIndicator: Int): PlayerController {
        layoutControl.setColorBackgroundProgressIndicator(colorBackgroundProgressIndicator = colorBackgroundProgressIndicator)
        return this
    }

    override fun setShowButtonScreenType(showButtonScreenType: Boolean): PlayerController {
        layoutControl.setShowButtonScreenType(showButtonScreenType = showButtonScreenType)
        return this
    }

    override fun addDownloadListener(downloadState: DownloadState): PlayerController {
        this.downloadState = downloadState
        layoutControl.setDownloadState(downloadState = downloadState)
        return this
    }


    override fun buildOnline() {
        val preferExtensionDecoders: Boolean = !BuildConfig.DEBUG
        val renderersFactory = PlayerUtil.buildRenderersFactory(context, preferExtensionDecoders)
        val downloadRequest = PlayerUtil.getDownloadTracker(context).getDownloadRequest(Uri.parse(playerResource?.mediaUrl))
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        downloadRequest?.let {
            playerResource?.let {
                downloadState?.onVideoHasBeenDownloaded(it)
                BroadcastSender.sendBroadcastDownloadStatus(context, DownloadEventModel(state = ConstantDownload.STATE_IS_DOWNLOADED,mediaUrl = playerResource!!.mediaUrl))
            }
        }
        val exoMediaSource = ExoMediaSource(context)
            .setPlayerResource(playerResource)
            .buildOnline()
        player = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .build()
        layoutControl.setPlayerView(player = player)
        layoutControl.setIsPlayOnline(true)
        player?.playWhenReady = true
        player?.trackSelectionParameters = trackSelectionParameters
        player?.setMediaSource(exoMediaSource)
        player?.addAnalyticsListener(analyticsListener)
        player?.trackSelector?.parameters = ParametersBuilder(context).setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
        player?.prepare()

        this.addView(layoutControl.getLayoutVideoPlayer())
    }

    override fun buildOffline() {
        val preferExtensionDecoders: Boolean = !BuildConfig.DEBUG
        val renderersFactory = PlayerUtil.buildRenderersFactory(context, preferExtensionDecoders)
        val downloadRequest = PlayerUtil.getDownloadTracker(context).getDownloadRequest(Uri.parse(playerResource?.mediaUrl))
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        downloadRequest?.let {
            playerResource?.let {
                downloadState?.onVideoHasBeenDownloaded(it)
                BroadcastSender.sendBroadcastDownloadStatus(context, DownloadEventModel(state = ConstantDownload.STATE_IS_DOWNLOADED,mediaUrl = playerResource!!.mediaUrl))
            }
        }
        val exoMediaSource = ExoMediaSource(context)
            .setPlayerResource(playerResource)
            .buildOffline()
        player = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .build()
        layoutControl.setPlayerView(player = player)
        layoutControl.setIsPlayOnline(false)
        player?.playWhenReady = true
        player?.trackSelectionParameters = trackSelectionParameters
        exoMediaSource?.let { player?.setMediaSource(it) }
        player?.addAnalyticsListener(analyticsListener)
        player?.trackSelector?.parameters = ParametersBuilder(context).setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
        player?.prepare()
        this@AndroidPlayer.addView(layoutControl.getLayoutVideoPlayer())
    }

    override fun buildResume(player: ExoPlayer): PlayerController {
        val downloadRequest = PlayerUtil.getDownloadTracker(context).getDownloadRequest(Uri.parse(playerResource?.mediaUrl))
        downloadRequest?.let { playerResource?.let { downloadState?.onVideoHasBeenDownloaded(it) } }
        layoutControl.setPlayerView(player = player)
        player.addAnalyticsListener(analyticsListener)
        player.prepare()
        this.addView(layoutControl.getLayoutVideoPlayer())
        return this
    }

    override fun getPlayer():ExoPlayer? {
        return player
    }

    override fun pause() {
        player?.pause()
    }

    override fun play() {
        player?.play()
    }

    override fun release() {
        player?.release()

    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        player?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        player?.release()
    }

}