package com.example.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothchat.databinding.ActivityChatBinding
import com.example.bluetoothchat.recyclerviews.adapters.ChatRecyclerViewAdapter
import com.example.bluetoothchat.recyclerviews.adapters.ChatRecyclerViewAdapter.Companion.DIRECTION_RECEIVE
import com.example.bluetoothchat.recyclerviews.adapters.ChatRecyclerViewAdapter.Companion.DIRECTION_SEND
import com.example.bluetoothchat.thread.ClientCommunicationThread
import com.example.bluetoothchat.thread.ClientCommunicationThread.Companion.CONNECT_CLOSE
import com.example.bluetoothchat.thread.ClientCommunicationThread.Companion.MESSAGE_READ
import com.example.bluetoothchat.thread.ClientCommunicationThread.Companion.MESSAGE_WRITE
import com.example.bluetoothchat.thread.ClientConnectThread
import com.example.bluetoothchat.thread.ClientConnectThread.Companion.CONNECT_FAIL
import com.example.bluetoothchat.thread.ClientConnectThread.Companion.CONNECT_SUCCESS

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var bluetoothDevice: BluetoothDevice? = null
    private val chatRecyclerViewAdapter by lazy { ChatRecyclerViewAdapter() }
    private var isButtonAccessible: Boolean = false
    private lateinit var messageHandler: Handler
    private var connectThread: ClientConnectThread? = null
    private var communicationThread: ClientCommunicationThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity ?????? ?????? bluetoothDevice ????????? ???????????????.
        bluetoothDevice = intent.getParcelableExtra("BLUETOOTH_DEVICE")

        binding.chatView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.chatView.adapter = chatRecyclerViewAdapter

        messageHandler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when(msg.what) {
                    CONNECT_SUCCESS -> { // ????????? ????????? ??????
                        binding.progressBarGroup.visibility = View.GONE

                        // ?????? ????????? ???????????? ???????????? ????????? ???????????????.
                        binding.imageView.setImageDrawable(
                            ContextCompat.getDrawable(this@ChatActivity, R.drawable.icon_remote_device_48)
                        )
                        binding.nameTextView.text = bluetoothDevice!!.name ?: bluetoothDevice!!.address
                        binding.addressTextView.text = bluetoothDevice!!.address

                        val rfcommSocket = msg.obj as BluetoothSocket
                        // Communication Thread ??? ???????????????.
                        communicationThread = ClientCommunicationThread(rfcommSocket, messageHandler)
                        communicationThread!!.start()
                    }

                    CONNECT_FAIL -> { // ????????? ????????? ??????
                        binding.progressBarGroup.visibility = View.GONE
                        binding.communicationLayout.visibility = View.GONE
                        binding.failTextView.visibility = View.VISIBLE
                    }

                    CONNECT_CLOSE -> { // ????????? ????????? ??????
                        // ??????????????? ????????? ????????? ????????? ????????? ??? ????????????.
                        // TODO
                        Toast.makeText(this@ChatActivity, "??????????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    MESSAGE_READ -> { // ?????? ????????????????????? ???????????? ?????? ??????
                        val receivedMessage = msg.obj as String
                        // ????????????????????? ?????? ???????????? ???????????????.
                        chatRecyclerViewAdapter.addItem(DIRECTION_RECEIVE, receivedMessage, System.currentTimeMillis())
                        binding.chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1) // ?????? ?????????
                    }

                    MESSAGE_WRITE -> { // ?????? ??????????????? ????????? ???????????? ????????? ??????
                        // ???????????? ????????? ?????? ????????? ????????? ??? ????????????.
                        // TODO
                    }
                }
            }
        }

        // ????????? ????????? ?????? ?????????
        binding.apply {
            chatView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if(bottom < oldBottom) {
                    if(chatRecyclerViewAdapter.itemCount > 0) {
                        chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
                    }
                }
            }
        }

        // editText ??? ????????? ????????? ???????????? ????????????
        binding.apply {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    text?.let {
                        if(it.isNotEmpty()) {
                            isButtonAccessible = true
                            sendBtnImageView.visibility = View.GONE
                            btnSend.visibility = View.VISIBLE
                        }
                        else {
                            isButtonAccessible = false
                            btnSend.visibility = View.GONE
                            sendBtnImageView.visibility = View.VISIBLE
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // ????????? ??????
        binding.apply {
            btnSend.setOnClickListener {
                // Update chat view
                if(isButtonAccessible) {
                    val sendMessage = editText.text.toString()
                    chatRecyclerViewAdapter.addItem(DIRECTION_SEND, sendMessage, System.currentTimeMillis())
                    chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1) // ?????? ?????????
                    editText.setText("")

                    communicationThread!!.write(sendMessage.toByteArray(Charsets.UTF_8))
                }
            }
        }

        connectThread = ClientConnectThread(bluetoothDevice!!, messageHandler)
        connectThread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        connectThread?.let { thread ->
            if(!thread.isInterrupted) {
                thread.interrupt()
                thread.cancel()
            }
        }
        communicationThread?.let { thread ->
            if(!thread.isInterrupted) {
                thread.interrupt()
                thread.cancel()
            }
        }
    }
}