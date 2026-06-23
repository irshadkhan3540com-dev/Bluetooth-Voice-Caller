package com.example.btcaller.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.btcaller.R
import com.example.btcaller.service.CallService

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // यहाँ फिक्स कर दिया है (: Bundle? हटा दिया)
        setContentView(R.layout.activity_call) // कॉलिंग स्क्रीन UI को जोड़ना

        val deviceName = intent.getStringExtra("REMOTE_DEVICE_NAME") ?: "अज्ञान डिवाइस"
        val deviceAddress = intent.getStringExtra("REMOTE_DEVICE_ADDRESS")
        
        // स्क्रीन पर सामने वाले डिवाइस का नाम सेट करना
        val callerNameText = findViewById<TextView>(R.id.callerNameText)
        callerNameText.text = deviceName

        // 1. बैकग्राउंड CallService को एक्टिव करना
        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)

        // 2. C++ वॉइस इंजन को एक्टिव करना और ब्लूटूथ कनेक्शन बनाना
        startNativeAudioConnection(deviceAddress)
    }

    // C++ (JNI) लेयर से सीधे बात करने वाले असली फंक्शन्स
    private external fun startNativeAudioConnection(address: String?)
    private external fun stopNativeAudioConnection()

    // जब यूजर स्क्रीन पर "कॉल समाप्त करें" बटन दबाएगा
    fun onDisconnectButtonClicked(view: View) {
        // 1. C++ इंजन को ब्लूटूथ सॉकेट बंद करने का सिग्नल देना
        stopNativeAudioConnection()

        // 2. बैकग्राउंड सर्विस को बंद करना
        val serviceIntent = Intent(this, CallService::class.java)
        stopService(serviceIntent)

        Toast.makeText(this, "कॉल समाप्त कर दी गई", Toast.LENGTH_SHORT).show()
        finish() // स्क्रीन बंद करना
    }

    override fun onDestroy() {
        super.onDestroy()
        stopNativeAudioConnection()
    }

    companion object {
        // हमारे C++ इंजन (CMakeLists) की बनाई हुई लाइब्रेरी को लोड करना
        init {
            System.loadLibrary("btcaller")
        }
    }
}

