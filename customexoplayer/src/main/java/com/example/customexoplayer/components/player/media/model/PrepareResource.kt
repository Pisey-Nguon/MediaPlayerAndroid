package com.example.customexoplayer.components.player.media.model

import android.net.Uri
import com.example.customexoplayer.components.utils.ResourceType

data class PrepareResource (val uri:Uri,val language:String? = null,val resourceType:ResourceType)