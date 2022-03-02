package com.example.bluetoothchat

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bluetoothchat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var bluetoothDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity 에서 받은 bluetoothDevice 객체를 저장합니다.
        bluetoothDevice = intent.getParcelableExtra("BLUETOOTH_DEVICE")

    }
}