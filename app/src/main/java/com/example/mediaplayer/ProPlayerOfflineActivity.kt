package com.example.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.customexoplayer.PlayerResource
import com.example.mediaplayer.databinding.ActivityProPlayerOfflineBinding


class ProPlayerOfflineActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityProPlayerOfflineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =ActivityProPlayerOfflineBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        title = "Professional Player Offline"
        val mediaUrl = "https://d2cqvl54b1gtkt.cloudfront.net/PRODUCTION/5d85da3fa81ada4c66211a07/post/media/video/1616987127933-bfc1a13a-49c6-4272-8ffd-dc04b05eed2c/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa.m3u8"
        val playerResource = PlayerResource(mediaName = "Transformer",mediaUrl = mediaUrl)

        mBinding.androidPlayer
            .setPlayerResource(playerResource)
            .setFullScreenEnabled(true)
            .setLifecycle(lifecycle)
            .buildOffline()
    }
}