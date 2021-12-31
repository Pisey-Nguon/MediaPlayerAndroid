package com.example.customexoplayer.components.utils

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive

object CoroutineUtil {
    fun startRepeatingJob(timeInterval: Long,executeTask:() -> Unit): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                executeTask.invoke()
                delay(timeInterval)
            }
        }
    }
}