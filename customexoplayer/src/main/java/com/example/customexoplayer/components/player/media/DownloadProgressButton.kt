package com.example.customexoplayer.components.player.media

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.customexoplayer.R
import com.google.android.exoplayer2.offline.Download
import com.google.android.material.progressindicator.CircularProgressIndicator

class DownloadProgressButton:FrameLayout {
    private var downloadProgressButtonCallback: DownloadProgressButtonCallback? = null
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val containerView = layoutInflater.inflate(R.layout.download_progress,this,false)
    private val icDownloadStatus:ImageView = containerView.findViewById(R.id.ic_download_status)
    private val progressBarDownloadStatus:CircularProgressIndicator = containerView.findViewById(R.id.progress_bar_download_status)
    private val containerDownloadProgress:RelativeLayout = containerView.findViewById(R.id.container_download_status)
    private val drawableStop = ContextCompat.getDrawable(context, R.drawable.ic_download_status_stop)
    private val drawableDownward = ContextCompat.getDrawable(context, R.drawable.ic_download_status_downward)
    private val drawableDone = ContextCompat.getDrawable(context, R.drawable.ic_download_status_done)
    private val drawableFailed = ContextCompat.getDrawable(context, R.drawable.ic_download_status_failed)


    constructor(context: Context):super(context){
        init()
    }
    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet){
        init()
    }
    constructor(context: Context,attributeSet: AttributeSet,defStyleAttr:Int):super(context,attributeSet,defStyleAttr){
        init()
    }

    private fun init(){
        icDownloadStatus.tag = -1
        containerDownloadProgress.setOnClickListener {
            when(icDownloadStatus.tag){
                -1 -> downloadProgressButtonCallback?.onStartDownload()
                Download.STATE_FAILED -> downloadProgressButtonCallback?.onStartDownload()
                Download.STATE_STOPPED -> downloadProgressButtonCallback?.onStartDownload()
                Download.STATE_REMOVING -> downloadProgressButtonCallback?.onStartDownload()
                Download.STATE_QUEUED -> downloadProgressButtonCallback?.onCancelDownload()
                Download.STATE_DOWNLOADING -> downloadProgressButtonCallback?.onCancelDownload()
                Download.STATE_RESTARTING -> downloadProgressButtonCallback?.onCancelDownload()
            }
        }
        this.addView(containerView)
    }

    fun downloadQueued(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableStop)
        progressBarDownloadStatus.isVisible = false
        progressBarDownloadStatus.isIndeterminate = true
        progressBarDownloadStatus.isVisible = true
    }

    fun downloadStarted(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableStop)
        progressBarDownloadStatus.isVisible = false
        progressBarDownloadStatus.isIndeterminate = false
        progressBarDownloadStatus.isVisible = true
    }


    fun downloadCompleted(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableDone)
        progressBarDownloadStatus.hide()
    }

    fun downloadFailed(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableFailed)
        progressBarDownloadStatus.hide()
    }

    fun downloadRemoved(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableDownward)
        progressBarDownloadStatus.hide()
    }

    fun downloadRestarted(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableStop)
        progressBarDownloadStatus.isVisible = false
        progressBarDownloadStatus.isIndeterminate = true
        progressBarDownloadStatus.isVisible = true
    }

    fun downloadStopped(state:Int){
        icDownloadStatus.tag = state
        icDownloadStatus.setImageDrawable(drawableDownward)
        progressBarDownloadStatus.hide()
    }

    fun setProgressDownload(progress:Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBarDownloadStatus.setProgress(progress,true)
        }else{
            progressBarDownloadStatus.progress = progress
        }
    }

    fun addListener(downloadProgressButtonCallback: DownloadProgressButtonCallback){
        this.downloadProgressButtonCallback = downloadProgressButtonCallback
    }



}

interface DownloadProgressButtonCallback{

    fun onStartDownload()
    fun onCancelDownload()
}