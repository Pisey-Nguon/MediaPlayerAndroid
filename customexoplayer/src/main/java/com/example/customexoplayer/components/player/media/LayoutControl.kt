package com.example.customexoplayer.components.player.media

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.customexoplayer.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.customexoplayer.AndroidPlayer
import com.example.customexoplayer.components.dialog.controller.PlayerBottomSheetController
import com.example.customexoplayer.components.player.download.download_service.DownloadMethod
import com.example.customexoplayer.components.player.download.model.DownloadEventModel
import com.example.customexoplayer.components.utils.ConstantDownload
import com.example.customexoplayer.components.utils.SystemUIUtils

@SuppressLint("CustomViewStyleable", "InflateParams")
class LayoutControl(private val context: Context, private val androidPlayer: AndroidPlayer, attributeSet: AttributeSet?)  {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val playerBottomSheetController: PlayerBottomSheetController = PlayerBottomSheetController(context)
    private val layoutVideoPlayer: View = layoutInflater.inflate(R.layout.layout_video_player,null)
    private val playerControlView: PlayerControlView = layoutVideoPlayer.findViewById(R.id.exo_controller)
    private val playerView: PlayerView = layoutVideoPlayer.findViewById(R.id.playerView)
    private val loadingView: CircularProgressIndicator =  layoutVideoPlayer.findViewById(R.id.loadingView)
    private val btnPlayView:ImageButton = layoutVideoPlayer.findViewById(R.id.exo_play)
    private val btnPauseView:ImageButton = layoutVideoPlayer.findViewById(R.id.exo_pause)
    private val btnViewType:ImageButton = layoutVideoPlayer.findViewById(R.id.exo_view_type)
    private val btnMore:ImageButton = layoutVideoPlayer.findViewById(R.id.exo_more)
    private val btnDownloadProgressButton: DownloadProgressButton = layoutVideoPlayer.findViewById(R.id.exo_download_progress)
    private val containerButtonProgress: FrameLayout = layoutVideoPlayer.findViewById(R.id.containerButtonProgress)

    private val a = context.obtainStyledAttributes(attributeSet, R.styleable.AndroidPlayer, 0, 0)
    //resource styled attributes
    private var resizingEnabled:Boolean = a.getBoolean(R.styleable.AndroidPlayer_resizingEnabled,false)
    private var iconPlay:Drawable = ContextCompat.getDrawable(context,a.getResourceId(R.styleable.AndroidPlayer_iconPlay,R.drawable.ic_play))!!
    private var iconPause:Drawable = ContextCompat.getDrawable(context,a.getResourceId(R.styleable.AndroidPlayer_iconPause,R.drawable.ic_pause))!!
    private var iconReplay:Drawable = ContextCompat.getDrawable(context,a.getResourceId(R.styleable.AndroidPlayer_iconReplay,R.drawable.ic_replay))!!
    private var colorBackgroundProgressIndicator:Int = ContextCompat.getColor(context,a.getResourceId(R.styleable.AndroidPlayer_colorBackgroundProgressIndicator,R.color.color_video_transparent))
    private var showButtonScreenType:Boolean = a.getBoolean(R.styleable.AndroidPlayer_showButtonScreenType,false)

    //Download method
    private val downloadMethod = DownloadMethod(context)

    //Callback
    private var downloadState: DownloadState? = null

    //references
    private var isPlayOnline = true

    init {
        a.recycle()

        //init styled attributes to UI
        btnPlayView.setImageDrawable(iconPlay)
        btnPauseView.setImageDrawable(iconPause)
        playerControlView.setBackgroundColor(colorBackgroundProgressIndicator)

        initEventClickListener()
        initBroadcastFromDownloadService()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initEventClickListener(){
        (playerView.context as AppCompatActivity).also { activity ->
            var prevWidth = -1
            var prevHeight = -1
            btnViewType.setOnClickListener {
                if(StoreInstance.exoViewType == ExoViewType.MANUAL_SCREEN){
                    StoreInstance.exoViewType = ExoViewType.FULL_SCREEN
                    SystemUIUtils.hideSystemBars(activity)
                    activity.supportActionBar?.hide()
                    prevWidth = androidPlayer.layoutParams.width
                    prevHeight = androidPlayer.layoutParams.height
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    androidPlayer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    androidPlayer.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutVideoPlayer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutVideoPlayer.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    androidPlayer.requestLayout()
                }else{
                    StoreInstance.exoViewType = ExoViewType.MANUAL_SCREEN
                    SystemUIUtils.showSystemBars(activity)
                    activity.supportActionBar?.show()
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    androidPlayer.layoutParams.height = prevHeight
                    androidPlayer.layoutParams.width = prevWidth
                    androidPlayer.requestLayout()
                }
            }
        }
        btnMore.setOnClickListener {
            androidPlayer.getPlayer()?.let { player -> playerBottomSheetController.showMoreDialog(player,isPlayOnline,androidPlayer.getPlayerResource()) }
        }
        btnDownloadProgressButton.addListener(object: DownloadProgressButtonCallback {

            override fun onStartDownload() {
                androidPlayer.getPlayer()?.let { player ->
                    playerBottomSheetController.showOptionDownloadVideoDialog(player,listener = {
                        val trackIndexMovie = it.id?.toInt()
                        downloadMethod.startDownload(androidPlayer.getPlayerResource(),trackIndexMovie!!)
                    })
                }
            }

            override fun onCancelDownload() {
                downloadMethod.cancelDownload()
            }
        })
    }

    private fun initBroadcastFromDownloadService(){
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ConstantDownload.ACTION_DOWNLOAD_STATUS -> {
                        val downloadEventModel = intent.getParcelableExtra<DownloadEventModel>(
                            ConstantDownload.DATA_DOWNLOAD_STATUS)
                        when(downloadEventModel?.state){
                            Download.STATE_DOWNLOADING -> {
                                btnDownloadProgressButton.downloadStarted(downloadEventModel.state)
                                androidPlayer.getPlayerResource().let { it?.let { it1 -> downloadState?.onDownloadStarted(it1) } }
                            }
                            Download.STATE_COMPLETED -> {
                                btnDownloadProgressButton.downloadCompleted(downloadEventModel.state)
                                androidPlayer.getPlayerResource().let { it?.let { it1 -> downloadState?.onDownloadCompleted(it1) } }
                            }
                            ConstantDownload.STATE_IS_DOWNLOADED -> {
                                btnDownloadProgressButton.downloadCompleted(downloadEventModel.state)
                            }
                            Download.STATE_FAILED -> {
                                btnDownloadProgressButton.downloadFailed(downloadEventModel.state)
                                androidPlayer.getPlayerResource().let { it?.let { it1 -> downloadState?.onDownloadFailed(it1) } }
                            }
                            Download.STATE_QUEUED -> btnDownloadProgressButton.downloadQueued(downloadEventModel.state)
                            Download.STATE_REMOVING -> btnDownloadProgressButton.downloadRemoved(downloadEventModel.state)
                            Download.STATE_RESTARTING -> btnDownloadProgressButton.downloadRestarted(downloadEventModel.state)
                            Download.STATE_STOPPED -> btnDownloadProgressButton.downloadStopped(downloadEventModel.state)
                        }
                    }
                    ConstantDownload.ACTION_DOWNLOAD_PERCENTAGE -> {
                        val downloadPercentage = intent.getIntExtra(ConstantDownload.DATA_DOWNLOAD_PERCENTAGE,0)
                        btnDownloadProgressButton.setProgressDownload(downloadPercentage)
                    }
                    else -> {
                        throw IllegalStateException("Unexpected value: " + intent?.action)
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConstantDownload.ACTION_DOWNLOAD_STATUS)
        intentFilter.addAction(ConstantDownload.ACTION_DOWNLOAD_PERCENTAGE)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun setDownloadState(downloadState: DownloadState){
        this.downloadState = downloadState
    }


    fun showLoading() {
        loadingView.show()
        btnPauseView.setImageDrawable(null)
        btnPlayView.setImageDrawable(null)
    }

    fun hideLoading() {
        loadingView.gone()
        btnPauseView.setImageDrawable(iconPause)
        btnPlayView.setImageDrawable(iconPlay)
    }

    fun setIsPlayOnline(isPlayOnline:Boolean){
        this.isPlayOnline = isPlayOnline
        if (isPlayOnline){
            btnDownloadProgressButton.show()
        }else{
            btnDownloadProgressButton.gone()
        }
    }

    fun setPlayerView(player: ExoPlayer?) {
        playerView.player = player
    }

    fun getLayoutVideoPlayer(): View {
        return layoutVideoPlayer
    }

    fun setResizingEnabled(resizingEnabled: Boolean) {
        this.resizingEnabled = resizingEnabled
    }

    fun setIconPlay(iconPlay: Drawable) {
        this.iconPlay = iconPlay
    }

    fun setIconPause(iconPause: Drawable) {
        this.iconPause = iconPause
    }

    fun setIconReplay(iconReplay: Drawable) {
        this.iconReplay = iconReplay
    }

    fun setColorBackgroundProgressIndicator(colorBackgroundProgressIndicator: Int) {
        this.colorBackgroundProgressIndicator = colorBackgroundProgressIndicator
    }

    fun setShowButtonScreenType(showButtonScreenType:Boolean){
        this.showButtonScreenType = showButtonScreenType
    }

    fun onSizeChanged(width: Int, height: Int) {
        if (showButtonScreenType){
            btnViewType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_full_screen))
            when(StoreInstance.exoViewType){
                ExoViewType.FULL_SCREEN -> btnViewType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_full_screen_exit))
                ExoViewType.MANUAL_SCREEN -> btnViewType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_full_screen))
            }
            btnViewType.show()
        }else{
            btnViewType.gone()
        }
    }

    fun onWrapHeight(videoSize: VideoSize) {
        if(resizingEnabled && StoreInstance.exoViewType != ExoViewType.FULL_SCREEN){
            val aspectRatioVideo = videoSize.height.toFloat() / videoSize.width.toFloat()
            val fromHeight = androidPlayer.measuredHeight
            val toHeight = androidPlayer.measuredWidth.toFloat() * aspectRatioVideo
            val animator = ValueAnimator.ofInt(fromHeight, toHeight.toInt())
            animator.addUpdateListener { valueAnimator ->
                val resizingHeight = valueAnimator.animatedValue as Int
                val layoutVideoPlayerParams = layoutVideoPlayer.layoutParams
                layoutVideoPlayerParams.height = resizingHeight
                layoutVideoPlayer.layoutParams = layoutVideoPlayerParams

                val androidPlayerParams = androidPlayer.layoutParams
                androidPlayerParams.height = resizingHeight
                androidPlayer.layoutParams = androidPlayerParams
            }
            animator.duration = 500
            animator.start()
        }
    }

    fun onReadyToPlay() {
        if(btnPlayView.drawable != iconPlay){
            btnPlayView.setImageDrawable(iconPlay)
        }
        btnMore.show()
        containerButtonProgress.show()
    }

    fun onEndPlay() {
        btnPlayView.setImageDrawable(iconReplay)
    }

}