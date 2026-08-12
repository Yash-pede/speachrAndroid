package com.yash.Speachr.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.yash.Speachr.R
import com.yash.Speachr.repository.AudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class FloatingService : Service(), KoinComponent {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var bubbleCard: CardView
    private lateinit var bubbleLogo: ImageView
    private lateinit var glowView: View

    private val audioRepository: AudioRepository by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var isRecording = false
    private var pulseAnimator: AnimatorSet? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        showFloatingBubble()
    }

    private fun startForegroundNotification() {
        val channelId = "VoiceOverlayChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Voice Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Speachr is active")
            .setContentText("Listening for your voice...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
    }

    private var isLongPressActive = false

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.BOTTOM or Gravity.END
        params.x = 20
        params.y = 300

        bubbleCard = floatingView.findViewById(R.id.bubble_card)
        bubbleLogo = floatingView.findViewById(R.id.bubble_logo)
        glowView = floatingView.findViewById(R.id.glow_view)

        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (!isLongPressActive) {
                        toggleRecording()
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    isLongPressActive = true
                    if (!isRecording) {
                        startRecordingUI()
                    }
                }
            })

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // Pass events to GestureDetector for clicks/long-presses
                gestureDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isLongPressActive = false
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (isLongPressActive && isRecording) {
                            stopRecording()
                            isLongPressActive = false
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Calculate move distance
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()

                        // Update params based on gravity. If gravity is BOTTOM|END,
                        // increasing params.x moves it LEFT, increasing params.y moves it UP.
                        params.x = initialX - deltaX
                        params.y = initialY - deltaY

                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
    }

    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (isRecording) return
        isRecording = true
        Log.d("Speachr", "Recording Started")

        startRecordingUI()

        try {
            audioFile = File(externalCacheDir, "recording.3gp")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("Speachr", "MediaRecorder prepare() failed", e)
            stopRecording()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        Log.d("Speachr", "Recording Stopped")

        stopRecordingUI()

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            // Upload the file
            audioFile?.let { file ->
                serviceScope.launch {
                    val result = audioRepository.transcribeAudio(file)
                    if (result != null) {
                        Log.d("Speachr", "Transcription: $result")
                        PasteAccessibilityService.pasteText(result.text)
                    } else {
                        Log.e("Speachr", "Transcription failed")
                        PasteAccessibilityService.pasteText("\uD83D\uDE1E Error")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Speachr", "MediaRecorder stop failed", e)
        }
    }

    private fun startRecordingUI() {
        Log.d("Speachr", "Recording UI Started")

        // Switch icon and background
        bubbleLogo.setImageResource(android.R.drawable.ic_btn_speak_now)
        bubbleCard.setCardBackgroundColor(Color.parseColor("#40FEF2F2")) // Light translucent red

        // Animate size increase
        animateScale(1.15f)
        startPulsing()
    }

    private fun stopRecordingUI() {
        Log.d("Speachr", "Recording UI Stopped")

        // Switch back to app icon and transparency
        bubbleLogo.setImageResource(R.mipmap.ic_launcher)
        bubbleCard.setCardBackgroundColor(Color.TRANSPARENT)

        // Animate size decrease
        animateScale(1.0f)
        stopPulsing()
    }

    private fun animateScale(scale: Float) {
        bubbleCard.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun startPulsing() {
        glowView.visibility = View.VISIBLE
        glowView.alpha = 0f

        // Optional: Force the tint to a nice recording red programmatically
        glowView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF3333"))

        // Card is 80dp, Glow is 160dp.
        // Scale 0.5 = 80dp (same size as card)
        // Scale 1.0 = 160dp (twice as big as card)
        val scaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 0.5f, 1.0f)
        val scaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 0.5f, 1.0f)

        // Fade in and out smoothly
        val alpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.1f, 0.5f)

        // Apply the repeating bounce effect to all animations
        listOf(scaleX, scaleY, alpha).forEach { animator ->
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
        }

        pulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1500 // Slower, more gentle pulse
            start()
        }
    }

    private fun stopPulsing() {
        pulseAnimator?.cancel()
        glowView.animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(200)
            .withEndAction { glowView.visibility = View.INVISIBLE }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPulsing()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
