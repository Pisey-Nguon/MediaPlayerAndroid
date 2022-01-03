package com.example.customexoplayer.components.player.media

import com.example.customexoplayer.components.player.media.model.AdResource

interface AdController {

    fun setAdResources(adResources:ArrayList<AdResource>):AdController
    fun buildWithAd()
    fun pauseAd()
}