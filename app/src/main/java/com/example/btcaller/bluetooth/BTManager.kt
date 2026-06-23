package com.example.btcaller.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context

class BTManager(private val context: Context) {

    // Fix: यहाँ 'as?' लगाया है ताकि ऐप क्रैश न हो
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    // 1. चेक करना कि फोन में ब्लूटूथ ऑन है या ऑफ
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    // 2. आस-पास के नए ब्लूटूथ डिवाइसेज को खोजना (Scan) शुरू करना
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery() // अगर पहले से स्कैन चल रहा है, तो उसे रीस्टार्ट करो
        }
        bluetoothAdapter?.startDiscovery()
    }

    // 3. ब्लूटूथ स्कैनिंग को रोकना (ताकि बैटरी और प्रोसेसर पर लोड न पड़े)
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    // 4. जो फोन पहले से ब्लूटूथ से पेयर (Connected) हैं, उनकी लिस्ट निकालना
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
}
