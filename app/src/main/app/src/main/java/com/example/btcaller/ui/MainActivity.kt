package com.example.btcaller.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // जिन-जिन अनुमतियों (Permissions) की जरूरत है, उनकी लिस्ट
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState: Bundle?)
        
        // ऐप खुलते ही सबसे पहले चेक करो कि परमिशन मिली हैं या नहीं
        if (checkPermissions()) {
            onPermissionsGranted()
        } else {
            // अगर नहीं मिली, तो यूजर के स्क्रीन पर पॉपअप दिखाओ
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    // परमिशन चेक करने का फंक्शन
    private fun checkPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // जब यूजर पॉपअप में Allow या Deny करेगा, तब यह फंक्शन चलेगा
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionsGranted()
            } else {
                // अगर यूजर मना कर देता है
                Toast.makeText(this, "कॉलिंग के लिए ब्लूटूथ और माइक की परमिशन जरूरी है!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // जब सारी परमिशन मिल जाएंगी, तब क्या करना है
    private fun onPermissionsGranted() {
        Toast.makeText(this, "सभी परमिशन मिल चुकी हैं! ब्लूटूथ तैयार है।", Toast.LENGTH_SHORT).show()
        // यहाँ से हम आगे ब्लूटूथ सर्च करने का लॉजिक शुरू करेंगे
    }
}

