package com.example.customexoplayer.components.utils

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity


object SystemUIUtils {

    fun hideSystemBars(activity: FragmentActivity) {
        if (Build.VERSION.SDK_INT >= 30){
            val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }else{
// Hide the status bar.
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

            activity.actionBar?.hide()
        }
    }

    fun showSystemBars(activity: AppCompatActivity){
        if (Build.VERSION.SDK_INT >= 30){
            val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }else{
// Hide the status bar.
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
            activity.actionBar?.show()
        }
    }
}