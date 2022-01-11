package com.example.customexoplayer

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.video.VideoSize
import com.example.customexoplayer.components.player.download.model.DownloadEventModel
import com.example.customexoplayer.components.player.media.*
import com.example.customexoplayer.components.player.media.model.AdResource
import com.example.customexoplayer.components.utils.*
import java.io.IOException
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AndroidPlayer: FrameLayout, PlayerController, ActionPlayerControl, DefaultLifecycleObserver {


    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var trackSelectionParameters: DefaultTrackSelector.Parameters
    private lateinit var layoutControl: LayoutControl
    private var player:ExoPlayer? = null
    private var adPlayer:ExoPlayer? = null
    private var downloadState: DownloadState? = null
    private var playerResource: PlayerResource? = null
    private var adResources:ArrayList<AdResource> ? = null
    private var repeatJob:Job? = null
    private val TAG = this.javaClass.name

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
                    repeatJob?.cancel()
                    layoutControl.hideLoading()
                    layoutControl.onEndPlay()
                }
            }
        }
    }

    private val analyticsAdListener = object: AnalyticsListener{
        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            when(state){
                Player.STATE_IDLE ->{
                    layoutControl.hideLoading()
                    player?.play()
                }
                Player.STATE_BUFFERING -> {
                    layoutControl.showLoading()
                    layoutControl.pauseCountDownSkip()
                }
                Player.STATE_READY ->{
                    layoutControl.hideLoading()
                    layoutControl.resumeCountDownSkip()
                }
                Player.STATE_ENDED ->{
                    layoutControl.hideLoading()
                    layoutControl.hideAd()
                    player?.play()
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

    private fun validateAd() {
        repeatJob = CoroutineUtil.startRepeatingJob(1000) {
            (context as Activity).runOnUiThread {
                adResources?.forEach { adResource ->
                    if(!adResource.isAlreadyShow){
                        val currentPositionSecond = player?.currentPosition?.let { TimeUnit.MILLISECONDS.toSeconds(it) }
                        if (currentPositionSecond ?: return@forEach == adResource.startPositionSecond){
                            adResource.isAlreadyShow = true
                            adPlayer?.setMediaItem(MediaItem.fromUri(adResource.adUrl))
                            adPlayer?.playWhenReady = true
                            adPlayer?.prepare()
                            player?.pause()
                            layoutControl.showAd()
                            layoutControl.initSkipView(adResource)
                            return@forEach
                        }
                    }
                }
            }
        }
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

    override fun setDownloadEnabled(downloadEnabled: Boolean): PlayerController {
        layoutControl.setDownloadEnabled(downloadEnabled = downloadEnabled)
        return this
    }

    override fun setFullScreenEnabled(fullScreenEnabled: Boolean): PlayerController {
        layoutControl.setFullScreenEnabled(fullScreenEnabled = fullScreenEnabled)
        return this
    }

    override fun setMoreOptionEnabled(moreOptionEnabled: Boolean): PlayerController {
        layoutControl.setMoreOptionEnabled(moreOptionEnabled = moreOptionEnabled)
        return this
    }

    override fun setTimeBarEnabled(timeBarEnabled: Boolean): PlayerController {
        layoutControl.setTimeBarEnabled(timeBarEnabled = timeBarEnabled)
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
        layoutControl.setPlayerForPlayerView(player = player)
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
        layoutControl.setPlayerForPlayerView(player = player)
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
        layoutControl.setPlayerForPlayerView(player = player)
        player.addAnalyticsListener(analyticsListener)
        player.prepare()
        this.addView(layoutControl.getLayoutVideoPlayer())
        return this
    }

    override fun buildWithAd() {
        val preferExtensionDecoders: Boolean = !BuildConfig.DEBUG
        val renderersFactory = PlayerUtil.buildRenderersFactory(context, preferExtensionDecoders)
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val downloadRequest = PlayerUtil.getDownloadTracker(context).getDownloadRequest(Uri.parse(playerResource?.mediaUrl))
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
        player?.playWhenReady = true
        player?.trackSelectionParameters = trackSelectionParameters
        player?.setMediaSource(exoMediaSource)
        player?.addAnalyticsListener(analyticsListener)
        player?.trackSelector?.parameters = ParametersBuilder(context).setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
        player?.prepare()

        adPlayer = ExoPlayer.Builder(context).build()
        adPlayer?.addAnalyticsListener(analyticsAdListener)
        layoutControl.setPlayerForPlayerView(player = player)
        layoutControl.setIsPlayOnline(true)
        layoutControl.setPlayerForAdsLoaderView(adPlayer)
        validateAd()
        this.addView(layoutControl.getLayoutVideoPlayer())
    }

    override fun pauseAd() {
        adPlayer?.pause()
    }

    override fun getPlayer():ExoPlayer? {
        return player
    }

    override fun setAdResources(adResources: ArrayList<AdResource>): AdController {
        this.adResources = adResources
        this.adResources?.sortWith(comparator = { s1: AdResource, s2: AdResource -> (s1.startPositionSecond - s2.startPositionSecond).toInt() })
        return this
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
        adPlayer?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        player?.release()
        adPlayer?.release()
        repeatJob?.cancel()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        adPlayer?.play()
    }

}