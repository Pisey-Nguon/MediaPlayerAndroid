package com.example.customexoplayer.components.dialog.viewholder

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customexoplayer.R
import com.example.customexoplayer.components.dialog.adapter.PlayerOptionAdapterCallback
import com.example.customexoplayer.components.dialog.model.PlayerOptionModel

class PlayerMoreItemHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    private val containerItemMore = itemView.findViewById<LinearLayout>(R.id.containerItemMore)
    private val titleItemMore = itemView.findViewById<TextView>(R.id.titleItemMore)
    private val iconItemMore = itemView.findViewById<ImageView>(R.id.iconItemMore)
    fun bind(playerOptionModel: PlayerOptionModel, listener: PlayerOptionAdapterCallback?){
        titleItemMore.text = playerOptionModel.title
        iconItemMore.setImageDrawable(playerOptionModel.value as Drawable)
        containerItemMore.setOnClickListener {
            listener?.onClicked(playerOptionModel)
        }
    }
}