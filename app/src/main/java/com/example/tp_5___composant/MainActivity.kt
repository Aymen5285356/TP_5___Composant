package com.example.tp_5___composant

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val channelId = "heads_up_notification_channel"
    private var pendingDelayedAction = false

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                if (pendingDelayedAction) {
                    scheduleDelayedNotification(5000)
                } else {
                    showNotification()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permission de notification refusée",
                    Toast.LENGTH_SHORT
                ).show()
            }
            pendingDelayedAction = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        findViewById<Button>(R.id.notifyButton).setOnClickListener {
            handleNotificationTrigger(isDelayed = false)
        }

        findViewById<Button>(R.id.notifyDelayedButton).setOnClickListener {
            handleNotificationTrigger(isDelayed = true)
        }
    }

    private fun handleNotificationTrigger(isDelayed: Boolean) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDelayedAction = isDelayed
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (isDelayed) {
                scheduleDelayedNotification(5000)
            } else {
                showNotification()
            }
        }
    }

    private fun scheduleDelayedNotification(delayMs: Long) {
        Toast.makeText(
            this,
            "Notification dans ${delayMs / 1000}s. Ouvrez une autre application !",
            Toast.LENGTH_LONG
        ).show()

        Handler(Looper.getMainLooper()).postDelayed({
            showNotification()
        }, delayMs)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Heads-Up Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for high priority heads-up banner notifications"
            enableVibration(true)
            enableLights(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun showNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nouveau message (style LinkedIn)")
            .setContentText("Vous avez reçu une notification en cours d'utilisation d'une autre application !")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
