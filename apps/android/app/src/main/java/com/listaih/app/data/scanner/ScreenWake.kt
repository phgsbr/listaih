package com.listaih.app.data.scanner

import android.app.Activity
import android.content.Context
import android.view.WindowManager

/**
 * Screen wake helpers for the post-scan popup flow (ANDROID-PLAN Fase 6, 6.3).
 * Only the "unrecognized" popup wakes the screen — recognized popups stay
 * discreet and do NOT turn the screen on (6.2).
 */
object ScreenWake {

    /**
     * Turns the screen on and shows the window even with keyguard.
     * Called when an unrecognized barcode requires user action.
     */
    fun wake(context: Context) {
        val activity = context.findActivity() ?: return
        activity.runOnUiThread {
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    /** Removes the wake flags (call when the popup is resolved/dismissed). */
    fun clear(context: Context) {
        val activity = context.findActivity() ?: return
        activity.runOnUiThread {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}