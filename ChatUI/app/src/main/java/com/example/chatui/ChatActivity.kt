package com.example.chatui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatui.databinding.ActivityChatBinding
import com.example.chatui.recyclerviews.adapters.ChatRecyclerViewAdapter

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private val chatRecyclerViewAdapter by lazy { ChatRecyclerViewAdapter() }
    private var isButtonAccessible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        deviceName = intent.getStringExtra("DEVICE_NAME")
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        binding.apply {
            // set chatView adapter
            chatView.layoutManager = LinearLayoutManager(this@ChatActivity)
            chatView.adapter = chatRecyclerViewAdapter

            // Test stub
            // set device panel
            imageView.setImageDrawable(ContextCompat.getDrawable(this@ChatActivity, R.drawable.icon_remote_device_48))
            nameTextView.text = deviceName!!
            addressTextView.text = deviceAddress!!
        }

        // ... 블루투스 통신 생략... //
    }

    override fun onResume() {
        super.onResume()

        // 키보드 팝업시 자동 스크롤
        binding.apply {
            chatView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if(bottom < oldBottom) {
                    if(chatRecyclerViewAdapter.itemCount > 0) {
                        chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
                    }
                }
            }
        }

        // editText 의 내용이 있으면 전송버튼 사용가능
        binding.apply {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                    text?.let {
                        if(it.length > 0) {
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

        // Send message to remote device
        binding.apply {
            btnSend.setOnClickListener {
                // Update chat view
                if(isButtonAccessible) {
                    val sendMessage = editText.text.toString()
                    // 보내는 방향
                    chatRecyclerViewAdapter.addItem(ChatRecyclerViewAdapter.DIRECTION_SEND, sendMessage, System.currentTimeMillis())

                    editText.setText("")
                    chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1) // 자동 스크롤

                    // 받는 방향
                    val receivedMessage = sendMessage // Sample
                    chatRecyclerViewAdapter.addItem(ChatRecyclerViewAdapter.DIRECTION_RECEIVE, receivedMessage, System.currentTimeMillis())
                    chatView.smoothScrollToPosition(chatRecyclerViewAdapter.itemCount-1)
                }
            }
        }
    }
}