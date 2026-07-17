package com.example.floatingquicklauncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayButton: Button? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        createNotificationChannel()
        startAsForegroundService()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlay()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.overlay_service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startAsForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOverlay() {
        if (overlayButton != null) return
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val minimumSize = (56 * resources.displayMetrics.density).toInt()
        val margin = (24 * resources.displayMetrics.density).toInt()
        val button = Button(this).apply {
            text = getString(R.string.overlay_button_label)
            contentDescription = getString(R.string.overlay_button_description)
            minWidth = minimumSize
            minHeight = minimumSize
            setOnClickListener { launchCalendar() }
        }
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = margin
            y = margin * 5
        }

        try {
            windowManager.addView(button, layoutParams)
            overlayButton = button
        } catch (_: SecurityException) {
            stopSelf()
        } catch (_: WindowManager.BadTokenException) {
            stopSelf()
        }
    }

    private fun removeOverlay() {
        val button = overlayButton ?: return
        try {
            windowManager.removeView(button)
        } catch (_: IllegalArgumentException) {
            // The system may already have detached the view after permission loss.
        } finally {
            overlayButton = null
        }
    }

    private fun launchCalendar() {
        try {
            startActivity(createCalendarIntent())
        } catch (_: ActivityNotFoundException) {
            showLaunchError()
        } catch (_: SecurityException) {
            showLaunchError()
        }
    }

    private fun showLaunchError() {
        Toast.makeText(this, R.string.external_app_unavailable, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    private fun overlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun createNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.overlay_service_notification_title))
            .setContentText(getString(R.string.overlay_service_notification_text))
            .setContentIntent(openAppIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "overlay_service"
        const val NOTIFICATION_ID = 1
    }
}

internal fun createCalendarIntent(): Intent {
    return Intent.makeMainSelectorActivity(
        Intent.ACTION_MAIN,
        Intent.CATEGORY_APP_CALENDAR,
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
