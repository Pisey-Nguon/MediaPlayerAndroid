package com.example.customexoplayer.components.player.custom.skipad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.example.customexoplayer.R
import com.example.customexoplayer.components.player.media.model.AdResource
import com.example.customexoplayer.components.utils.CoroutineUtil
import com.example.customexoplayer.components.utils.gone
import com.example.customexoplayer.components.utils.show
import kotlinx.coroutines.*
import java.util.*

class SkipView:FrameLayout,SkipController {

    private lateinit var containerAdFirstShow:LinearLayout
    private lateinit var containerAdSecondShow:LinearLayout
    private lateinit var tvCountDownAd:TextView
    private lateinit var ivThumbAd:ImageView
    private lateinit var btnSkip:AppCompatButton

    private var adResource:AdResource? = null
    private var onSkipped:(()-> Unit)? = null
    private var repeatJob:Job? = null
    private var repeatTimer:Timer? = null
    constructor(context:Context):super(context){
        init()
    }
    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet){
        init()
    }
    constructor(context: Context,attributeSet: AttributeSet,defStyleAttr:Int):super(context,attributeSet,defStyleAttr){
        init()
    }

    private fun init(){
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val skipView: View = layoutInflater.inflate(R.layout.skip_view,this,false)
        containerAdFirstShow = skipView.findViewById(R.id.containerAdFirstShow)
        containerAdSecondShow = skipView.findViewById(R.id.containerAdSecondShow)
        tvCountDownAd = skipView.findViewById(R.id.tvCountDownAd)
        ivThumbAd = skipView.findViewById(R.id.ivThumbAd)
        btnSkip = skipView.findViewById(R.id.btnSkip)
        initEventClickListener()
        addView(skipView)
    }
    private fun initEventClickListener(){
        btnSkip.setOnClickListener {
            onSkipped?.invoke()
        }
    }
    private fun showFirstAd(){
        containerAdFirstShow.show()
        containerAdSecondShow.gone()
    }
    private fun showSecondAd(){
        containerAdFirstShow.gone()
        containerAdSecondShow.show()
    }
    private fun hideBothContainer(){
        containerAdFirstShow.gone()
        containerAdSecondShow.gone()
    }


    override fun setAdResource(adResource: AdResource?):SkipController {
        this.adResource = adResource
        return this
    }

    override fun setCountDown(duration: String) {
        tvCountDownAd.text = duration
    }

    override fun addButtonSkipListener(onSkipped: () -> Unit):SkipController {
        this.onSkipped = onSkipped
        return this
    }

    override fun pauseCountDown() {
        repeatTimer?.cancel()
    }

    override fun resumeCountDown() {
        repeatTimer?.purge()
    }

    @SuppressLint("CheckResult")
    override fun build() {
        if (adResource?.isEnableSkip == true){
            Glide.with(context).load(adResource?.thumbnailUrl).into(ivThumbAd)
            if (adResource?.durationEnableSkipSecond != null && adResource?.durationEnableSkipSecond!! > 0){
                showFirstAd()
            }else{
                showSecondAd()
            }
            var remainDuration = adResource?.durationEnableSkipSecond
            repeatTimer = Timer()
            val hourlyTask: TimerTask = object : TimerTask() {
                override fun run() {
                    Log.d("statusJob", "run: ")
                    (context as Activity).runOnUiThread {
                        tvCountDownAd.text = "$remainDuration"
                        if (remainDuration == 0){
                            showSecondAd()
                            repeatJob?.cancel()
                        }
                        remainDuration = remainDuration?.minus(1)
                    }

                }
            }
            repeatTimer?.schedule(hourlyTask, 0L, 1000)
        }else{
            hideBothContainer()
        }
    }
}