package com.bookmate.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Starts the app again in a new task and closes the current one. Used after logout so that no
 * screen state or ViewModel of the previous user stays in memory.
 */
fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}