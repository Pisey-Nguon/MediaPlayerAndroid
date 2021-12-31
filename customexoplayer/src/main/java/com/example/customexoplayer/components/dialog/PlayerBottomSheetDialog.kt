package com.example.customexoplayer.components.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customexoplayer.R
import com.example.customexoplayer.components.dialog.adapter.PlayerOptionAdapter
import com.example.customexoplayer.components.dialog.adapter.PlayerOptionAdapterCallback
import com.example.customexoplayer.components.dialog.model.PlayerOptionModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.customexoplayer.components.utils.ExoViewType
import com.example.customexoplayer.components.utils.StoreInstance
import com.example.customexoplayer.components.utils.SystemUIUtils

class PlayerBottomSheetDialog(private val playerOptionList:ArrayList<PlayerOptionModel>) : BottomSheetDialogFragment() {

    private var playerOptionAdapter: PlayerOptionAdapter? = null

    init {
        playerOptionAdapter = PlayerOptionAdapter(playerOptionList)
    }

    fun addListener(listener: PlayerOptionAdapterCallback){
        playerOptionAdapter?.addListener(listener)
    }

    fun selectedItem(playerOptionModel: PlayerOptionModel){
        playerOptionList.forEach {
            it.isSelected = false
        }
        val index = playerOptionList.indexOf(playerOptionModel)
        playerOptionList[index].isSelected = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.player_bottom_sheet_dialog,container,false)
    }

    override fun onStart() {
        super.onStart()
        //this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (StoreInstance.exoViewType == ExoViewType.FULL_SCREEN){
            if (Build.VERSION.SDK_INT >= 30){
                dialog?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }
        }
        val rvBottomSheet = view.findViewById<RecyclerView>(R.id.rvBottomSheet)
        rvBottomSheet.layoutManager = LinearLayoutManager(view.context)
        rvBottomSheet.adapter = playerOptionAdapter

    }
    override fun onDestroy() {
        super.onDestroy()
        if (StoreInstance.exoViewType == ExoViewType.FULL_SCREEN){
            activity?.let { SystemUIUtils.hideSystemBars(it) }
        }
    }

}