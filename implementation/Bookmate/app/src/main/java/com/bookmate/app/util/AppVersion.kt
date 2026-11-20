package com.bookmate.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** The version name of the installed app, or an empty text if it cannot be read. */
@Suppress("DEPRECATION")
fun appVersionName(context: Context): String = try {
    val manager = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= 33) {
        manager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        manager.getPackageInfo(context.packageName, 0)
    }
    info.versionName.orEmpty()
} catch (e: PackageManager.NameNotFoundException) {
    ""
}