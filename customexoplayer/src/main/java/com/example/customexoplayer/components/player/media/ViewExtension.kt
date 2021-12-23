package com.example.customexoplayer.components.player.media

import android.view.View


fun View.show(){
    if (this.visibility != View.VISIBLE){
        this.visibility = View.VISIBLE
    }
}

fun View.gone(){
    if (this.visibility != View.GONE){
        this.visibility = View.GONE
    }
}

fun View.invisible(){
    if (this.visibility != View.INVISIBLE){
        this.visibility = View.INVISIBLE
    }
}