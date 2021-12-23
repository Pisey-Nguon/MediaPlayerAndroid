package com.example.customexoplayer.components.dialog.controller

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.customexoplayer.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.util.MimeTypes
import com.example.customexoplayer.PlayerResource
import com.example.customexoplayer.components.dialog.PlayerBottomSheetDialog
import com.example.customexoplayer.components.dialog.adapter.PlayerOptionAdapterCallback
import com.example.customexoplayer.components.dialog.model.PlayerOptionModel
import com.example.customexoplayer.components.dialog.model.PlayerOptionViewHolderType
import com.example.customexoplayer.components.dialog.model.PlayerSelectedType
import java.util.*
import kotlin.collections.ArrayList

class PlayerBottomSheetController(private val context: Context) {
    private val qualityList = ArrayList<PlayerOptionModel>()
    private val subtitleList = ArrayList<PlayerOptionModel>()
    private val playbackSpeedList = ArrayList<PlayerOptionModel>()


    fun showMoreDialog(player: ExoPlayer,isPlayOnline:Boolean,playerResource: PlayerResource?){
        val moreOptionList = ArrayList<PlayerOptionModel>()
        if (isPlayOnline){
            moreOptionList.add(
                PlayerOptionModel(title = "Quality",value = ContextCompat.getDrawable(context,
                    R.drawable.ic_setting)!!,viewHolderViewHolderType = PlayerOptionViewHolderType.MORE,playerSelectType = PlayerSelectedType.QUALITY,isSelected = false)
            )
        }
        if (playerResource?.subtitles?.size != 0){
            moreOptionList.add(
                PlayerOptionModel(title = "Subtitle",value = ContextCompat.getDrawable(context,
                    R.drawable.ic_caption)!!,viewHolderViewHolderType = PlayerOptionViewHolderType.MORE,playerSelectType = PlayerSelectedType.SUBTITLE,isSelected = false)
            )
        }
        moreOptionList.add(
            PlayerOptionModel(title = "Playback speed",value = ContextCompat.getDrawable(context,
                R.drawable.ic_playback_speed)!!,viewHolderViewHolderType = PlayerOptionViewHolderType.MORE,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false)
        )
        val dialog = PlayerBottomSheetDialog(playerOptionList = moreOptionList)
        val listener = object : PlayerOptionAdapterCallback {
            override fun onClicked(playerOptionModel: PlayerOptionModel) {
                dialog.dismissAllowingStateLoss()
                when(playerOptionModel.playerSelectType){
                    PlayerSelectedType.QUALITY -> showChangingQualityDialog(player)
                    PlayerSelectedType.PLAYBACK_SPEED -> showChangingPlaybackSpeedDialog(player)
                    PlayerSelectedType.SUBTITLE -> showChangingSubtitleDialog(player)
                }
            }

        }
        dialog.addListener(listener = listener)
        dialog.show((context as AppCompatActivity).supportFragmentManager,null)
    }

    fun showChangingQualityDialog(player:ExoPlayer){
        if(qualityList.size == 0){
            val value = Format.Builder().build()
            qualityList.add(PlayerOptionModel(title = "Auto",value = value,viewHolderViewHolderType = PlayerOptionViewHolderType.QUALITY,playerSelectType = PlayerSelectedType.QUALITY,isSelected = true))
            player.currentTracksInfo.trackGroupInfos.forEachIndexed { index, trackGroupInfo ->
                for (i in 0 until trackGroupInfo.trackGroup.length){
                    val format = trackGroupInfo.trackGroup.getFormat(i)
                    if (MimeTypes.isVideo(format.sampleMimeType)){
                        qualityList.add(PlayerOptionModel(title = "${format.height}p",value = format,viewHolderViewHolderType = PlayerOptionViewHolderType.QUALITY,playerSelectType = PlayerSelectedType.QUALITY,isSelected = false))
                    }
                }

            }
        }

        val dialog = PlayerBottomSheetDialog(playerOptionList = qualityList)
        val listener = object : PlayerOptionAdapterCallback {
            override fun onClicked(playerOptionModel: PlayerOptionModel) {
                dialog.dismissAllowingStateLoss()
                dialog.selectedItem(playerOptionModel)
                val data = playerOptionModel.value as Format
                val width = data.width
                val height = data.height
                val bitrate = data.bitrate
                val parametersBuilder: DefaultTrackSelector.ParametersBuilder = (player.trackSelector as DefaultTrackSelector).buildUponParameters()
                if (width > 0 && height > 0) {
                    parametersBuilder.setMaxVideoSize(width, height)
                }
                if (bitrate > 0) {
                    parametersBuilder.setMaxVideoBitrate(bitrate)
                }
                if (width == -1 && height == -1 && bitrate == -1) {
                    parametersBuilder.clearVideoSizeConstraints()
                    parametersBuilder.setMaxVideoBitrate(Int.MAX_VALUE)
                }
                (player.trackSelector as DefaultTrackSelector).setParameters(parametersBuilder)
            }

        }
        dialog.addListener(listener = listener)
        dialog.show((context as AppCompatActivity).supportFragmentManager,null)
    }

    private fun changeSubtitleVideo(context:Context,language: String,trackSelector: DefaultTrackSelector){
        if (language == "Off"){
            trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context)
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
                .build()
        }else{
            val rendererIndex = 2
            val trackGroups = trackSelector.currentMappedTrackInfo!!.getTrackGroups(rendererIndex)
            for (t in 0 until trackGroups.length){
                for (f in 0 until trackGroups[t].length){
                    if (language.lowercase(Locale.ROOT) == trackGroups[t].getFormat(f).language){
                        val override = TrackSelectionOverrides.TrackSelectionOverride(trackGroups[t])
                        val trackSelectionOverrides = TrackSelectionOverrides.Builder().addOverride(override).build()
                        val tracker = DefaultTrackSelector.ParametersBuilder(context)
                            .setRendererDisabled(C.TRACK_TYPE_TEXT, false)
                            .setTrackSelectionOverrides(trackSelectionOverrides)
                            .build()
                        trackSelector.parameters = tracker
                    }
                }

            }
        }
    }
    fun showChangingSubtitleDialog(player: ExoPlayer){
        if(subtitleList.size == 0){
            subtitleList.add(PlayerOptionModel(title = "Off",value = "Off",viewHolderViewHolderType = PlayerOptionViewHolderType.SUBTITLE,playerSelectType = PlayerSelectedType.SUBTITLE,isSelected = true))
            player.currentTracksInfo.trackGroupInfos.forEachIndexed { index, trackGroupInfo ->
                for (i in 0 until trackGroupInfo.trackGroup.length){
                    val format = trackGroupInfo.trackGroup.getFormat(i)
                    if (MimeTypes.isText(format.sampleMimeType) && format.sampleMimeType != "application/cea-608"){
                        format.sampleMimeType
                        subtitleList.add(PlayerOptionModel(title = "${format.language?.replaceFirstChar(Char::uppercase)}",value = format.language.toString(),viewHolderViewHolderType = PlayerOptionViewHolderType.SUBTITLE,playerSelectType = PlayerSelectedType.SUBTITLE,isSelected = false))
                    }
                }
            }
        }
        val dialog = PlayerBottomSheetDialog(subtitleList)
        val listener = object: PlayerOptionAdapterCallback {
            override fun onClicked(playerOptionModel: PlayerOptionModel) {
                dialog.dismissAllowingStateLoss()
                dialog.selectedItem(playerOptionModel)
                val data = playerOptionModel.value as String
                changeSubtitleVideo(context,data,(player.trackSelector as DefaultTrackSelector))
            }
        }
        dialog.addListener(listener)
        dialog.show((context as AppCompatActivity).supportFragmentManager,null)
    }

    fun showChangingPlaybackSpeedDialog(player: ExoPlayer){
        if (playbackSpeedList.size == 0){
            playbackSpeedList.add(PlayerOptionModel(title = "0.5x",value = 0.5f,viewHolderViewHolderType = PlayerOptionViewHolderType.PLAYBACK_SPEED,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false))
            playbackSpeedList.add(PlayerOptionModel(title = "0.75x",value = 0.75f,viewHolderViewHolderType = PlayerOptionViewHolderType.PLAYBACK_SPEED,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false))
            playbackSpeedList.add(PlayerOptionModel(title = "Normal",value = 1f,viewHolderViewHolderType = PlayerOptionViewHolderType.PLAYBACK_SPEED,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false))
            playbackSpeedList.add(PlayerOptionModel(title = "1.25x",value = 1.25f,viewHolderViewHolderType = PlayerOptionViewHolderType.PLAYBACK_SPEED,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false))
            playbackSpeedList.add(PlayerOptionModel(title = "1.5x",value = 1.5f,viewHolderViewHolderType = PlayerOptionViewHolderType.PLAYBACK_SPEED,playerSelectType = PlayerSelectedType.PLAYBACK_SPEED,isSelected = false))
        }
        val dialog = PlayerBottomSheetDialog(playbackSpeedList)
        val listener = object: PlayerOptionAdapterCallback {
            override fun onClicked(playerOptionModel: PlayerOptionModel) {
                dialog.dismissAllowingStateLoss()
                dialog.selectedItem(playerOptionModel)
                val data = playerOptionModel.value as Float
                player.setPlaybackSpeed(data)
            }
        }
        dialog.addListener(listener)
        dialog.show((context as AppCompatActivity).supportFragmentManager,null)
    }

    fun showOptionDownloadVideoDialog(player: ExoPlayer,listener:(data:Format) -> Unit){
        val optionDownloadVideoList = ArrayList<PlayerOptionModel>()
        player.currentTracksInfo.trackGroupInfos.forEach { trackGroupInfo ->
            for (i in 0 until trackGroupInfo.trackGroup.length){
                val format = trackGroupInfo.trackGroup.getFormat(i)
                if (MimeTypes.isVideo(format.sampleMimeType)){
                    optionDownloadVideoList.add(PlayerOptionModel(title = "${format.height}p",value = format,viewHolderViewHolderType = PlayerOptionViewHolderType.QUALITY,playerSelectType = PlayerSelectedType.QUALITY,isSelected = false))
                }
            }
        }
        val dialog = PlayerBottomSheetDialog(optionDownloadVideoList)
        val listener = object: PlayerOptionAdapterCallback {
            override fun onClicked(playerOptionModel: PlayerOptionModel) {
                dialog.dismissAllowingStateLoss()
                val data = playerOptionModel.value as Format
                listener.invoke(data)
            }
        }
        dialog.addListener(listener)
        dialog.show((context as AppCompatActivity).supportFragmentManager,null)
    }
}