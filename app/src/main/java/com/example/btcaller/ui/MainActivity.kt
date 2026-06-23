package com.example.btcaller.ui

import android.Manifest
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.btcaller.R
import com.example.btcaller.bluetooth.BTManager

class MainActivity : AppCompatActivity() {

    private lateinit var btManager: BTManager
    private lateinit var scanButton: Button
    private lateinit var devicesListView: ListView
    private val deviceList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // डिवाइस मिलने पर यह रिसीवर काम करेगा
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                // परमिशन चेक के साथ डिवाइस का नाम निकालना
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    device?.name
                } else {
                    "Unknown Device"
                }
                
                val deviceInfo = "${deviceName ?: "Unknown"} - ${device?.address}"
                
                // लिस्ट में डिवाइस ऐड करना
                if (!deviceList.contains(deviceInfo)) {
                    deviceList.add(deviceInfo)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = BTManager(this)
        scanButton = findViewById(R.id.scanButton)
        devicesListView = findViewById(R.id.devicesListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        devicesListView.adapter = adapter

        // बटन दबाने पर स्कैनिंग चालू करना
        scanButton.setOnClickListener {
            deviceList.clear()
            adapter.notifyDataSetChanged()
            btManager.startDiscovery()
            Toast.makeText(this, "डिवाइस खोजना शुरू...", Toast.LENGTH_SHORT).show()
        }

        // Fix: Android 14 क्रैश से बचने के लिए
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        btManager.stopDiscovery()
    }
}

