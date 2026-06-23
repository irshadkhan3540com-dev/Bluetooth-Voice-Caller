package com.example.btcaller.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.btcaller.R
import com.example.btcaller.bluetooth.BTManager

class MainActivity : AppCompatActivity() {

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
    
    private lateinit var btManager: BTManager
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private val discoveredDevices = ArrayList<BluetoothDevice>()
    private val deviceStrings = ArrayList<String>()
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState: Bundle?)
        setContentView(R.layout.activity_main) // XML स्क्रीन को कोड से जोड़ना

        btManager = BTManager(this)

        statusText = findViewById(R.id.statusText)
        val scanButton = findViewById<Button>(R.id.scanButton)
        val devicesListView = findViewById<ListView>(R.id.devicesListView)

        // लिस्ट को स्क्रीन पर दिखाने के लिए अडैप्टर सेट करना
        deviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceStrings)
        devicesListView.adapter = deviceListAdapter

        // लिस्ट में किसी भी फोन के नाम पर क्लिक करते ही कॉलिंग स्क्रीन (CallActivity) खुल जाएगी
        devicesListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = discoveredDevices[position]
            btManager.stopDiscovery() // कॉल कनेक्ट करते समय स्कैनिंग बंद करना जरूरी है
            
            val intent = Intent(this, CallActivity::class.java).apply {
                putExtra("REMOTE_DEVICE_NAME", getDeviceNameSafe(selectedDevice))
                putExtra("REMOTE_DEVICE_ADDRESS", selectedDevice.address)
            }
            startActivity(intent)
        }

        // स्कैन बटन पर क्लिक का लॉजिक
        scanButton.setOnClickListener {
            if (checkPermissions()) {
                onPermissionsGranted()
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
            }
        }

        // नए ब्लूटूथ डिवाइस मिलने पर खबर देने वाला ब्रॉडकास्ट रिसीवर रजिस्टर करना
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    // ब्लूटूथ खोज (Discovery) का लाइव रिसीवर
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device)
                    val name = getDeviceNameSafe(device)
                    deviceStrings.add("$name\n${device.address}")
                    deviceListAdapter.notifyDataSetChanged() // स्क्रीन पर लिस्ट अपडेट करें
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceNameSafe(device: BluetoothDevice): String {
        return if (checkPermissions()) device.name ?: "अज्ञात डिवाइस" else "अज्ञात डिवाइस"
    }

    private fun checkPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionsGranted()
            } else {
                Toast.makeText(this, "ब्लूटूथ ऐप चलाने के लिए अनुमतियाँ जरूरी हैं!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onPermissionsGranted() {
        statusText.text = "आस-पास के डिवाइस खोजे जा रहे हैं..."
        discoveredDevices.clear()
        deviceStrings.clear()
        deviceListAdapter.notifyDataSetChanged()
        btManager.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        btManager.stopDiscovery()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            // अगर रिसीवर पहले से अनरजिस्टर है तो क्रैश न हो
        }
    }
}

