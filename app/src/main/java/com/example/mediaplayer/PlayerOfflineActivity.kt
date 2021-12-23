package com.example.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.customexoplayer.PlayerResource
import com.example.customexoplayer.SubtitleModel
import com.example.mediaplayer.databinding.ActivityPlayerOfflineBinding


class PlayerOfflineActivity : AppCompatActivity() {
    lateinit var mBinding:ActivityPlayerOfflineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPlayerOfflineBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val mediaUrl = "https://d2cqvl54b1gtkt.cloudfront.net/PRODUCTION/5d85da3fa81ada4c66211a07/post/media/video/1616987127933-bfc1a13a-49c6-4272-8ffd-dc04b05eed2c/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa.m3u8"
        val subtitles = ArrayList<SubtitleModel>()
        subtitles.add(SubtitleModel(subtitleUrl = "https://filebin.net/dr73cv9evxoquhqo/English_Transformers_The_Last_Knight_Official_Trailer_1_2017_Michael.srt",language = "English"))
        subtitles.add(SubtitleModel(subtitleUrl = "https://filebin.net/dr73cv9evxoquhqo/Khmer_Transformers_The_Last_Knight_Official_Trailer_1_2017_Michael.srt",language = "Khmer"))
        val playerResource = PlayerResource(mediaName = "Transformer",mediaUrl = mediaUrl,subtitles = subtitles)

        mBinding.androidPlayer
            .setPlayerResource(playerResource)
            .setShowButtonScreenType(true)
            .setLifecycle(lifecycle)
            .buildOffline()
    }
}