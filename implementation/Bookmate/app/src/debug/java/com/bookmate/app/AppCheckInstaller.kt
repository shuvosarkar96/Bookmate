package com.bookmate.app

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/** Debug builds only. Firebase prints a debug token in Logcat that must be registered in the console. */
object AppCheckInstaller {
    private const val TAG = "BookmateAppCheck"

    fun install(context: Context) {
        FirebaseApp.initializeApp(context)
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        // Asking for a token now makes the debug provider print its secret (tag DebugAppCheckProvider).
        appCheck.getAppCheckToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "App Check token OK")
            } else {
                Log.w(TAG, "App Check token failed: " + task.exception?.message)
            }
        }
    }
}