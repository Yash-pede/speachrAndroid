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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event == null) return

        when (event.eventType) {
            // Add TYPE_VIEW_CLICKED because sometimes users tap an already-focused text box
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                checkIfTextBoxIsActive()
            }
        }
    }

    private fun checkIfTextBoxIsActive() {
        // 1. Get the current top window the user is interacting with
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            stopService()
            return
        }

        // 2. Search the ENTIRE window for the specific element holding the keyboard focus
        val focusedInputNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        // 3. If we found one, and it is editable, the user is ready to type!
        if (focusedInputNode != null && focusedInputNode.isEditable) {
            Log.d(TAG, "Editable text field is currently active! Keeping bubble.")

            val serviceIntent = Intent(this, FloatingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            Log.d(TAG, "No editable field found on screen. Hiding bubble.")
            stopService()
        }

        // 4. Always recycle nodes to prevent memory leaks
        focusedInputNode?.recycle()
        rootNode.recycle()
    }


    private fun stopService() {
        stopService(Intent(this, FloatingService::class.java))
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
