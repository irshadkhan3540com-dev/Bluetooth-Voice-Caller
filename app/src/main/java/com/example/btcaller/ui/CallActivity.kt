package com.example.btcaller.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.btcaller.service.CallService

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState: Bundle?)
        
        // सामने वाले ब्लूटूथ डिवाइस का नाम और एड्रेस दूसरी स्क्रीन से यहाँ आएगा
        val deviceName = intent.getStringExtra("REMOTE_DEVICE_NAME") ?: "अज्ञात डिवाइस"
        val deviceAddress = intent.getStringExtra("REMOTE_DEVICE_ADDRESS")
        
        // स्क्रीन पर छोटा सा मैसेज दिखाओ कि कॉल जुड़ रही है
        Toast.makeText(this, "$deviceName के साथ कॉल कनेक्ट हो रही है...", Toast.LENGTH_LONG).show()

        // 1. जैसे ही कॉलिंग स्क्रीन खुलेगी, हम बैकग्राउंड 'CallService' को एक्टिव कर देंगे
        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)

        // 2. यहाँ पर हम C++ इंजन को कॉल कनेक्ट करने का सिग्नल भेजेंगे
        startNativeAudioConnection(deviceAddress)
    }

    // C++ इंजन से जुड़ने वाला काल्पनिक फंक्शन (इसे हम C++ कोड लिखते समय असली बनाएंगे)
    private fun startNativeAudioConnection(address: String?) {
        // यहाँ C++ (JNI) के जरिए ब्लूटूथ सॉकेट खुलेगा और माइक चालू होगा
    }

    // जब यूजर स्क्रीन पर "कॉल काटें" (Disconnect) बटन दबाएगा, तब यह फंक्शन चलेगा
    fun onDisconnectButtonClicked() {
        // 1. बैकग्राउंड सर्विस को बंद करो ताकि नोटिफिकेशन हट जाए
        val serviceIntent = Intent(this, CallService::class.java)
        stopService(serviceIntent)

        // 2. यूजर को बताओ और स्क्रीन बंद कर दो
        Toast.makeText(this, "कॉल काट दी गई है", Toast.LENGTH_SHORT).show()
        finish() 
    }

    override fun onBackPressed() {
        // अगर कॉल चल रही है, तो बैक बटन दबाने से कॉल कटेगी नहीं, सिर्फ स्क्रीन पीछे होगी
        super.onBackPressed()
    }
}

