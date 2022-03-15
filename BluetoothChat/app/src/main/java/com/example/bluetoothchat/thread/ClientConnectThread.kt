package com.example.bluetoothchat.thread

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import java.util.*

class ClientConnectThread(
    private val bluetoothDevice: BluetoothDevice,
    private val messageHandler: Handler
) : Thread() {
    private val rfcommSocket: BluetoothSocket by lazy {
        bluetoothDevice.createRfcommSocketToServiceRecord(
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        )
    }

    companion object {
        const val CONNECT_SUCCESS = 1
        const val CONNECT_FAIL = 0
    }

    override fun run() {
        try {
            rfcommSocket.connect()
            if(!isInterrupted) {
                val message = messageHandler.obtainMessage(CONNECT_SUCCESS, rfcommSocket)
                message.sendToTarget()
            }

        } catch (e: IOException) {
            // .. 연결에 실패한 경우 .. //
            cancel()
            if(!isInterrupted) {
                val message = messageHandler.obtainMessage(CONNECT_FAIL)
                message.sendToTarget()
            }
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { }
    }
}