package com.yash.Speachr.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class PasteAccessibilityService : AccessibilityService() {

    private val TAG = "PasteService"
    private val handler = Handler(Looper.getMainLooper())
    private var stopServiceRunnable: Runnable? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val sourceNode: AccessibilityNodeInfo? = event.source
                if (sourceNode?.isEditable == true) {
                    Log.d(TAG, "Editable text field focused")
                    cancelPendingStop()
                    val serviceIntent = Intent(this, FloatingService::class.java)
                    startForegroundService(serviceIntent)
                } else {
                    Log.d(TAG, "Focus lost from editable field")
//                    scheduleStopService()
                }
            }
        }
    }

    private fun scheduleStopService() {
        cancelPendingStop()
        val runnable = Runnable {
            Log.d(TAG, "Executing debounced stop service")
            stopService(Intent(this, FloatingService::class.java))
        }
        stopServiceRunnable = runnable
        handler.postDelayed(runnable, 500)
    }

    private fun cancelPendingStop() {
        stopServiceRunnable?.let {
            handler.removeCallbacks(it)
            stopServiceRunnable = null
        }
    }

    private fun pasteTestText(node: AccessibilityNodeInfo) {
        try {
            // Check if the text field is still active and valid in memory
            if (node.isEditable) {
                val arguments = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        "Hello from Speachr!"
                    )
                }

                // Injects the text directly into the selected field
                val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                Log.d(TAG, "Paste attempt status: $success")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error trying to paste text: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service Interrupted")
    }

    override fun onServiceConnected() {
        Log.d(TAG, "Speachr Accessibility Service Successfully Connected!")

        // Automatically bring the app back to front when service is enabled
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error returning to app: ${e.message}")
        }
    }
}
