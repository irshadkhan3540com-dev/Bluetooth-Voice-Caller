package com.example.btcaller.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class CallActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var device: BluetoothDevice? = null
    private var isConnected = false
    private var isMuted = false
    private var isOnSpeaker = false

    private lateinit var statusText: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnMute: Button
    private lateinit var btnSpeaker: Button
    private lateinit var btnEndCall: Button

    // SPP UUID - Standard Serial Port Profile
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            padding = 50
        }

        statusText = TextView(this).apply {
            text = "डिवाइस से कनेक्ट हो रहे हैं..."
            textSize = 18f
        }

        btnConnect = Button(this).apply {
            text = "कनेक्ट करें"
            textSize = 14f
            height = 100
        }

        btnMute = Button(this).apply {
            text = "🔊 म्यूट नहीं"
            textSize = 14f
            height = 100
        }

        btnSpeaker = Button(this).apply {
            text = "🔈 स्पीकर"
            textSize = 14f
            height = 100
        }

        btnEndCall = Button(this).apply {
            text = "कॉल समाप्त करें"
            textSize = 14f
            height = 100
        }

        layout.addView(statusText)
        layout.addView(btnConnect)
        layout.addView(btnMute)
        layout.addView(btnSpeaker)
        layout.addView(btnEndCall)

        setContentView(layout)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // डिवाइस एड्रेस प्राप्त करें
        val deviceAddress = intent.getStringExtra("device_address")
        val deviceName = intent.getStringExtra("device_name")

        if (deviceAddress != null) {
            device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            statusText.text = "डिवाइस: $deviceName\nस्टेटस: तैयार"
            
            // ऑटो कनेक्ट करें
            btnConnect.performClick()
        }

        btnConnect.setOnClickListener {
            if (isConnected) {
                disconnectDevice()
            } else {
                connectDevice()
            }
        }

        btnMute.setOnClickListener {
            toggleMute()
        }

        btnSpeaker.setOnClickListener {
            toggleSpeaker()
        }

        btnEndCall.setOnClickListener {
            endCall()
        }
    }

    private fun connectDevice() {
        if (device == null) {
            Toast.makeText(this, "डिवाइस नहीं मिला", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@Thread
                }

                // सॉकेट क्रिएट करें
                bluetoothSocket = device?.createRfcommSocketToServiceRecord(SPP_UUID)

                if (bluetoothSocket != null) {
                    bluetoothAdapter?.cancelDiscovery()
                    bluetoothSocket?.connect()

                    isConnected = true
                    runOnUiThread {
                        statusText.text = "✓ कनेक्टेड\nकॉल सक्रिय है..."
                        btnConnect.text = "डिसकनेक्ट करें"
                        Toast.makeText(this@CallActivity, "कनेक्ट हो गए!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                isConnected = false
                runOnUiThread {
                    statusText.text = "✗ कनेक्शन विफल\n${e.message}"
                    Toast.makeText(this@CallActivity, "कनेक्शन विफल: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun disconnectDevice() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket?.close()
                bluetoothSocket = null
            }
            isConnected = false
            statusText.text = "डिवाइस से डिसकनेक्ट हो गए"
            btnConnect.text = "दोबारा कनेक्ट करें"
        } catch (e: IOException) {
            Toast.makeText(this, "डिसकनेक्ट त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleMute() {
        if (!isConnected) {
            Toast.makeText(this, "पहले कनेक्ट करें", Toast.LENGTH_SHORT).show()
            return
        }

        isMuted = !isMuted
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (isMuted) {
            audioManager.setMicrophoneMute(true)
            btnMute.text = "🔇 म्यूट"
            Toast.makeText(this, "माइक म्यूट हो गया", Toast.LENGTH_SHORT).show()
        } else {
            audioManager.setMicrophoneMute(false)
            btnMute.text = "🔊 म्यूट नहीं"
            Toast.makeText(this, "माइक सक्रिय", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSpeaker() {
        if (!isConnected) {
            Toast.makeText(this, "पहले कनेक्ट करें", Toast.LENGTH_SHORT).show()
            return
        }

        isOnSpeaker = !isOnSpeaker
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (isOnSpeaker) {
            audioManager.speakerphoneOn = true
            btnSpeaker.text = "🔉 स्पीकर ऑन"
            Toast.makeText(this, "स्पीकर ऑन", Toast.LENGTH_SHORT).show()
        } else {
            audioManager.speakerphoneOn = false
            btnSpeaker.text = "🔈 स्पीकर ऑफ"
            Toast.makeText(this, "स्पीकर ऑफ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endCall() {
        disconnectDevice()
        statusText.text = "कॉल समाप्त हुआ"
        Toast.makeText(this, "कॉल खत्म", Toast.LENGTH_SHORT).show()
        
        // 1 सेकंड बाद होम स्क्रीन पर जाएं
        Thread {
            Thread.sleep(1000)
            finish()
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

