package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.customexoplayer.PlayerResource
import com.example.customexoplayer.SubtitleModel
import com.example.customexoplayer.components.player.media.DownloadState
import com.example.customexoplayer.components.player.media.model.AdResource
import com.example.mediaplayer.databinding.ActivityProPlayerOnlineBinding



class ProPlayerOnlineActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityProPlayerOnlineBinding
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProPlayerOnlineBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        title = "Professional Player Online"
        val mediaUrl = "https://d2cqvl54b1gtkt.cloudfront.net/PRODUCTION/5d85da3fa81ada4c66211a07/post/media/video/1616987127933-bfc1a13a-49c6-4272-8ffd-dc04b05eed2c/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa/1616987128057-740d153b431660cf976789c1901192a961f0fd5b2a2af43e2388f671fa03c2aa.m3u8"
        val adsUrl = "https://dtk8cvappka3r.cloudfront.net/DEVELOPMENT/5f913e0bcb29d61c4c6e680e/media/post/video/1640767849133-ce358b1b-97bb-4486-aed4-1df1c56e2096/1640767849133-3a185a5b2b68e0973518ea5b6f55bc50dc4b125d36240b815.mp4"
        val subtitles = ArrayList<SubtitleModel>()
        subtitles.add(SubtitleModel(subtitleUrl = "https://milio-media-dev.s3.ap-southeast-1.amazonaws.com/Admin-Wallet/English_Transformers_The_Last_Knight_Official_Trailer_1_2017_Michael.srt",language = "English"))
        subtitles.add(SubtitleModel(subtitleUrl = "https://milio-media-dev.s3.ap-southeast-1.amazonaws.com/Admin-Wallet/Khmer_Transformers_The_Last_Knight_Official_Trailer_1_2017_Michael.srt",language = "Khmer"))

        val thumbAd = "https://dtk8cvappka3r.cloudfront.net/DEVELOPMENT/5f913e0bcb29d61c4c6e680e/media/post/thumbnail/1640848563082-034b44d16077c165606041d1c652d481234324f6c6165076a40e48.jpeg"
        val adResource1 = AdResource(adUrl = adsUrl,thumbnailUrl = thumbAd,startPositionSecond = 10,durationEnableSkipSecond = 6,isEnableSkip = false)
        val adResource2 = AdResource(adUrl = adsUrl,thumbnailUrl = thumbAd,startPositionSecond = 15,durationEnableSkipSecond = 8,isEnableSkip = true)
        val adResource3 = AdResource(adUrl = adsUrl,thumbnailUrl = thumbAd,startPositionSecond = 30,durationEnableSkipSecond = 3,isEnableSkip = true)
        val adResources = ArrayList<AdResource>()
        adResources.add(adResource1)
        adResources.add(adResource2)
        adResources.add(adResource3)
        val playerResource = PlayerResource(mediaName = "Transformer",mediaUrl = mediaUrl,subtitles = subtitles)

        mBinding.androidPlayer
            .setPlayerResource(playerResource)
            .setLifecycle(lifecycle)
            .setMoreOptionEnabled(true)
            .setDownloadEnabled(true)
            .setFullScreenEnabled(true)
            .addDownloadListener(object: DownloadState {
                override fun onDownloadCompleted(playerResource: PlayerResource) {
                    mBinding.btnPlayOffline.visibility = View.VISIBLE
                }

                override fun onDownloadStarted(playerResource: PlayerResource) {

                }

                override fun onDownloadFailed(playerResource: PlayerResource) {

                }

                override fun onVideoHasBeenDownloaded(playerResource: PlayerResource) {
                    mBinding.btnPlayOffline.visibility = View.VISIBLE
                }

            })
            .setAdResources(adResources)
            .buildWithAd()

        mBinding.btnPlayOffline.setOnClickListener {
            startActivity(Intent(this, ProPlayerOfflineActivity::class.java))
        }
    }
}