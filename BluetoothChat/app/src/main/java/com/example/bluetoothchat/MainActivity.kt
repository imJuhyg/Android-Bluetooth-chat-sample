package com.example.bluetoothchat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothchat.recyclerviews.adapters.DeviceRecyclerViewAdapter
import com.example.bluetoothchat.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(this) }
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var hasPermission = false
    private lateinit var pairedDevices: ArrayList<BluetoothDevice>
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 페어링된 기기 목록을 나타내는 리사이클러 뷰 입니다.
        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter

        val animation = AnimationUtils.loadAnimation(this, R.anim.to_top_from_bottom_2)
        binding.pairedDeviceRecyclerView.animation = animation

        // bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() // Deprecated!
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        bluetoothAdapter ?: run {
            // 블루투스를 지원하지 않는 기기입니다.
            // TODO
            exitProcess(0)
        }

        /**
         * 애플리케이션이 API 31 이상을 대상으로 하는 경우
         * 블루투스 기능을 사용하기 위해서 BLUETOOTH_CONNECT 권한을 요청해야 합니다.
         */
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requestBluetoothPermission()
        }

        if(hasPermission && !bluetoothAdapter!!.isEnabled) { // 블루투스 활성화를 요청합니다.
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(intent)
        }

        // 페어링된 디바이스를 얻습니다.
        pairedDevices = ArrayList(bluetoothAdapter!!.bondedDevices)
        // 페어링된 디바이스를 리사이클러 뷰에 추가합니다.
        for(pairedDevice in pairedDevices) {
            deviceRecyclerViewAdapter.addItem(
                pairedDevice.name ?: pairedDevice.address,
                pairedDevice.address
            )
        }

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_CANCELED) {
                // 페어링된 디바이스 목록 새로고침
                deviceRecyclerViewAdapter.clear()
                pairedDevices = ArrayList(bluetoothAdapter!!.bondedDevices)
                for(pairedDevice in pairedDevices) {
                    deviceRecyclerViewAdapter.addItem(
                        pairedDevice.name ?: pairedDevice.address,
                        pairedDevice.address
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 블루투스 인텐트
        binding.btnSearch.setOnClickListener {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            activityLauncher.launch(intent)
        }

        // 디바이스 연결 요청
        deviceRecyclerViewAdapter.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val recyclerViewItem = deviceRecyclerViewAdapter.getItem(position)

                for(pairedDevice in pairedDevices) {
                    // 선택된 디바이스를 ChatActivity 로 넘겨줍니다.
                    if(pairedDevice.address == recyclerViewItem.deviceAddress) {
                        val intent = Intent(this@MainActivity, ChatActivity::class.java)
                        intent.putExtra("BLUETOOTH_DEVICE", pairedDevice)
                        startActivity(intent)
                    }
                }
            }
        })
    }

    @RequiresApi(31)
    private fun requestBluetoothPermission() {
        // 요청된 권한에 대한 결과를 얻습니다.
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                // BLUETOOTH_CONNECT 권한을 얻은 경우입니다.
                hasPermission = true

            } else {
                // BLUETOOTH_CONNECT 권한을 얻지 못한 경우입니다.
                Toast.makeText(this, "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            // BLUETOOTH_CONNECT 권한을 요청합니다.
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

        } else {
            // BLUETOOTH_CONNECT 권한이 이미 있는 경우입니다.
            hasPermission = true
        }
    }
}