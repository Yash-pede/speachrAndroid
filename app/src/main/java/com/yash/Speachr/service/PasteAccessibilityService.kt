package com.yash.Speachr.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class PasteAccessibilityService : AccessibilityService() {

    private val TAG = "PasteService"

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Safe check for null event
        if (event == null) return

        // Check if the event is a focus or click on an input box
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            val sourceNode: AccessibilityNodeInfo? = event.source ?: return

            // Verify if the active element is actually an editable text field
            if (sourceNode?.isEditable == true) {
                Log.d(TAG, "User clicked or focused inside a text box!")

                // Wait 2 seconds (2000 milliseconds) and then paste text automatically
                Handler(Looper.getMainLooper()).postDelayed({
                    pasteTestText(sourceNode)
                }, 2000)
            }
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
    }
}
