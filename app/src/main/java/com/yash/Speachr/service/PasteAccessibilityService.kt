package com.yash.Speachr.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
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

    companion object {

        @SuppressLint("StaticFieldLeak")
        var instance: PasteAccessibilityService? = null
        fun pasteText(text: String) {
            val service = instance ?: return
            try {
                val rootNode = service.rootInActiveWindow ?: return

                val focusedInputNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

                if (focusedInputNode != null && focusedInputNode.isEditable) {
                    var existingText = focusedInputNode.text?.toString() ?: ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusedInputNode.isShowingHintText) {
                        existingText = ""
                    }
                    val cursorStart = focusedInputNode.textSelectionStart
                    val cursorEnd = focusedInputNode.textSelectionEnd

                    val combinedText =
                        if (cursorStart in 0..existingText.length && cursorEnd in 0..existingText.length) {
                            // If the cursor is valid, slice the text into two parts
                            val realStart = minOf(cursorStart, cursorEnd)
                            val realEnd = maxOf(cursorStart, cursorEnd)

                            val beforeCursor = existingText.substring(0, realStart)
                            val afterCursor = existingText.substring(realEnd)

                            // Sandwich our new text in the middle!
                            // (Adding a space before the new text if needed)
                            val space =
                                if (beforeCursor.isNotEmpty() && !beforeCursor.endsWith(" ")) " " else ""
                            "$beforeCursor$space$text$space$afterCursor"
                        } else {
                            // Fallback: If no cursor is found, just append to the end
                            if (existingText.isNotEmpty()) "$existingText $text" else text
                        }

                    val arguments = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            combinedText
                        )
                    }

                    val success = focusedInputNode.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT, arguments
                    )
                    Log.d("paste", "Paste attempt status: $success")
                }
            } catch (e: Exception) {
                Log.e("paste", "Error trying to paste text: ${e.message}")
            }
        }

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event == null) return

        when (event.eventType) {
            // Add TYPE_VIEW_CLICKED because sometimes users tap an already-focused text box
            AccessibilityEvent.TYPE_VIEW_FOCUSED, AccessibilityEvent.TYPE_VIEW_CLICKED, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                checkIfTextBoxIsActive()
            }
        }
    }

    private fun checkIfTextBoxIsActive() {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            stopService()
            return
        }

        val focusedInputNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

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

        focusedInputNode?.recycle()
        rootNode.recycle()
    }


    private fun stopService() {
        stopService(Intent(this, FloatingService::class.java))
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

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
