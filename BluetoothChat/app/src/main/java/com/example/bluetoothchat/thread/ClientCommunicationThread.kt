package com.example.bluetoothchat.thread

import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ClientCommunicationThread(
    private val rfcommSocket: BluetoothSocket,
    private val messageHandler: Handler
) : Thread() {
    private val inputStream: InputStream = rfcommSocket.inputStream
    private val outputStream: OutputStream = rfcommSocket.outputStream
    private val readBuffer: ByteArray = ByteArray(1024) // Buffer size

    companion object {
        const val MESSAGE_READ: Int = 101
        const val MESSAGE_WRITE: Int = 102
        const val CONNECT_CLOSE: Int = 100
    }

    override fun run() {
        var messageLength: Int
        while (true) {
            if(isInterrupted) break
            messageLength = try {
                inputStream.read(readBuffer) // readBuffer 에 메시지를 읽습니다.

            } catch (e: IOException) { // 연결이 끊기면 발생
                cancel()
                val message = messageHandler.obtainMessage(CONNECT_CLOSE)
                message.sendToTarget()
                break
            }

            // 읽은 메시지를 메인 스레드로 전달합니다.
            val receivedMessage = String(readBuffer.copyOf(messageLength))
            val message = messageHandler.obtainMessage(MESSAGE_READ, receivedMessage)
            message.sendToTarget()
        }
    }

    fun write(bytes: ByteArray) {
        try {
            outputStream.write(bytes) // 연결된 블루투스 디바이스로 메시지를 전달합니다.

            // 전송 성공 메시지를 메인 스레드로 전달합니다.
            val message = messageHandler.obtainMessage(MESSAGE_WRITE, String(bytes, Charsets.UTF_8))
            message.sendToTarget()

        } catch (e: IOException) {
            cancel()
        }
    }

    fun cancel() {
        try { rfcommSocket.close() } catch (e: IOException) { }
    }
}