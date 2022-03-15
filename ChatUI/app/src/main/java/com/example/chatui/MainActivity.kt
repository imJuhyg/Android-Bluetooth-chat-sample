package com.example.chatui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatui.databinding.ActivityMainBinding
import com.example.chatui.recyclerviews.adapters.DeviceRecyclerViewAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter

        val animation = AnimationUtils.loadAnimation(this, R.anim.to_top_from_bottom_2)
        binding.pairedDeviceRecyclerView.animation = animation

        // Sample Data
        deviceRecyclerViewAdapter.addItem("Remote device", "11:AA:22:BB:33:CC")
    }

    override fun onResume() {
        super.onResume()

        // Paired devices click event
        deviceRecyclerViewAdapter.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // ... //
                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                intent.putExtra("DEVICE_NAME", "Remote device")
                intent.putExtra("DEVICE_ADDRESS", "11:AA:22:BB:33:CC")
                startActivity(intent)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        deviceRecyclerViewAdapter.clear()
    }
}