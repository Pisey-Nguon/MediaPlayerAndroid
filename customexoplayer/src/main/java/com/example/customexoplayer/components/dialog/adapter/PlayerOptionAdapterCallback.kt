package com.example.customexoplayer.components.dialog.adapter

import com.example.customexoplayer.components.dialog.model.PlayerOptionModel

interface PlayerOptionAdapterCallback {

    fun onClicked(playerOptionModel: PlayerOptionModel)
}