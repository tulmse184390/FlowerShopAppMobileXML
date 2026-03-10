package com.example.flowershopapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.flowershopapp.R
import com.example.flowershopapp.ui.cart.CartActivity

object CartBadgeHelper {

    private const val CHANNEL_ID = "cart_notification_channel"
    private const val BADGE_CHANNEL_ID = "cart_badge_channel"
    private const val NOTIFICATION_ID = 1001
    private const val BADGE_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Channel for "item added" pop-up notifications
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cart Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shows the number of items in your cart"
        }
        manager.createNotificationChannel(channel)

        // Channel for the silent persistent badge notification
        val badgeChannel = NotificationChannel(
            BADGE_CHANNEL_ID,
            "Cart Badge",
            NotificationManager.IMPORTANCE_LOW   // silent – no sound or vibration
        ).apply {
            description = "Keeps the cart item count visible on the app icon"
            setShowBadge(true)
            enableLights(false)
            enableVibration(false)
        }
        manager.createNotificationChannel(badgeChannel)
    }

    fun updateBadge(badgeView: TextView, count: Int) {
        if (count > 0) {
            badgeView.visibility = android.view.View.VISIBLE
            badgeView.text = if (count > 99) "99+" else count.toString()
        } else {
            badgeView.visibility = android.view.View.GONE
        }
    }

    /**
     * Updates the launcher app-icon badge with the current cart item count.
     * Posts a silent persistent notification (IMPORTANCE_LOW – no sound/vibration)
     * using NotificationCompat so Android keeps the badge alive.
     * Cancels the notification (removes badge) when count reaches 0.
     */
    fun updateAppIconBadge(context: Context, count: Int) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (count <= 0) {
            notificationManager.cancel(BADGE_NOTIFICATION_ID)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, CartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val itemLabel = if (count == 1) "1 item" else "$count items"
        val expandedText = "You have $itemLabel in your cart. Tap to view or checkout."

        val notification = NotificationCompat.Builder(context, BADGE_CHANNEL_ID)
            // ── Icon displayed in the status bar and on the app icon badge ──
            .setSmallIcon(R.drawable.ic_cart)
            // ── Title shows the count clearly, e.g. "🛒 Cart · 3 items" ──
            .setContentTitle("🛒 Cart · $itemLabel")
            .setContentText(expandedText)
            // ── setNumber() is the key: tells the launcher how many items
            //    to display as the badge count (Samsung One UI, MIUI, Huawei etc.) ──
            .setNumber(count)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // ── Expanded (long-press or pull-down) view ──
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedText)
                    .setBigContentTitle("🛒 Cart · $itemLabel")
            )
            // ── Silent & persistent so the badge stays alive ──
            .setPriority(NotificationCompat.PRIORITY_LOW)  // works with IMPORTANCE_LOW channel
            .setOngoing(true)          // cannot be swiped away; stays until cart is empty
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)    // no repeated sound / vibration on re-posts
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(BADGE_NOTIFICATION_ID, notification)

    }

    fun showCartNotification(context: Context, itemCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, CartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cart)
            .setContentTitle("Flower Shop Cart")
            .setContentText("You have $itemCount item(s) in your cart")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancelCartNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}

