package com.example.customexoplayer.components.player.media.model

data class AdResource(val adUrl:String, val thumbnailUrl:String, val startPositionSecond:Long, val durationEnableSkipSecond:Int, val isEnableSkip:Boolean, var isAlreadyShow:Boolean = false)
