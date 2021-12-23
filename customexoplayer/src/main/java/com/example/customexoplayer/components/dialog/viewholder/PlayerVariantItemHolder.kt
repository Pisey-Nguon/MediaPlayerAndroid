package com.example.customexoplayer.components.dialog.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customexoplayer.R
import com.example.customexoplayer.components.dialog.adapter.PlayerOptionAdapterCallback
import com.example.customexoplayer.components.dialog.model.PlayerOptionModel
import com.example.customexoplayer.components.player.media.invisible
import com.example.customexoplayer.components.player.media.show

class PlayerVariantItemHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    private val containerItemVariant = itemView.findViewById<LinearLayout>(R.id.containerItemVariant)
    private val titleItemVariant = itemView.findViewById<TextView>(R.id.titleItemVariant)
    private val selectedIconItemVariant = itemView.findViewById<ImageView>(R.id.selectedIconItemVariant)
    fun bind(playerOptionModel: PlayerOptionModel, listener: PlayerOptionAdapterCallback?){
        titleItemVariant.text = playerOptionModel.title
        if(playerOptionModel.isSelected){
            selectedIconItemVariant.show()
        }else{
            selectedIconItemVariant.invisible()
        }
        containerItemVariant.setOnClickListener {
            listener?.onClicked(playerOptionModel)
        }
    }
}