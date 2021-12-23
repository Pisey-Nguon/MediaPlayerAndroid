package com.example.customexoplayer.components.dialog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customexoplayer.R
import com.example.customexoplayer.components.dialog.model.PlayerOptionModel
import com.example.customexoplayer.components.dialog.model.PlayerOptionViewHolderType
import com.example.customexoplayer.components.dialog.viewholder.PlayerMoreItemHolder
import com.example.customexoplayer.components.dialog.viewholder.PlayerVariantItemHolder

class PlayerOptionAdapter(private val playerOptionList:ArrayList<PlayerOptionModel>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listener: PlayerOptionAdapterCallback? = null
    companion object{
        const val moreType = 0
        const val variantType = 1
    }

    fun addListener(listener: PlayerOptionAdapterCallback){
        this.listener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when(playerOptionList[position].viewHolderViewHolderType){
            PlayerOptionViewHolderType.MORE -> moreType
            PlayerOptionViewHolderType.QUALITY -> variantType
            PlayerOptionViewHolderType.PLAYBACK_SPEED -> variantType
            PlayerOptionViewHolderType.SUBTITLE -> variantType
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            moreType -> PlayerMoreItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_player_more,parent,false))
            variantType -> PlayerVariantItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_player_variant,parent,false))
            else -> throw IllegalArgumentException("not found view holder")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is PlayerVariantItemHolder -> holder.bind(playerOptionModel = playerOptionList[position],listener = listener)
            is PlayerMoreItemHolder -> holder.bind(playerOptionModel = playerOptionList[position],listener = listener)
        }
    }

    override fun getItemCount(): Int {
        return playerOptionList.size
    }
}