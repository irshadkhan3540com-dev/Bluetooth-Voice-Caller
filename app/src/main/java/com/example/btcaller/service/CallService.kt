package com.example.btcaller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CallService : Service() {

    private val CHANNEL_ID = "BT_CALL_CHANNEL"
    private val NOTIFICATION_ID = 112

    override fun onCreate() {
        super.onCreate()
        // सर्विस शुरू होते ही सबसे पहले नोटिफिकेशन चैनल बनाओ (Android 8.0+ के लिए जरूरी)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // जैसे ही कॉल शुरू होगी, स्क्रीन पर एक "Active Call" का नोटिफिकेशन दिखेगा
        val notification = createNotification("ब्लूटूथ कॉल चालू है...")
        
        // एंड्रॉइड को बताओ कि यह एक foreground service है जो फोन कॉल संभाल रही है
        startForeground(NOTIFICATION_ID, notification)

        // यहाँ पर हम बाद में C++ इंजन को एक्टिव करने का कोड जोड़ेंगे

        return START_STICKY // अगर सिस्टम मेमोरी कम होने पर इसे बंद करे, तो दोबारा अपने आप चालू कर दे
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // हमें इसे किसी एक्टिविटी से बांधना नहीं है, यह स्वतंत्र चलेगी
    }

    // नोटिफिकेशन बनाने का फंक्शन
    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BT Voice Caller")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call) // एंड्रॉइड का डिफ़ॉल्ट कॉल आइकॉन
            .setOngoing(true) // यूजर इस नोटिफिकेशन को स्वाइप करके हटा नहीं सकता जब तक कॉल चालू है
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    // नोटिफिकेशन चैनल रजिस्टर करना
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bluetooth Call Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "यह चैनल ब्लूटूथ कॉलिंग के दौरान सर्विस को एक्टिव रखता है।"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // जब कॉल कट जाएगी, तो सर्विस बंद हो जाएगी
        stopForeground(true)
    }
}

