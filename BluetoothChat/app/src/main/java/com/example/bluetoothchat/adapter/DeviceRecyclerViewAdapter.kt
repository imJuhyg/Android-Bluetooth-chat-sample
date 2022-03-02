package com.example.bluetoothchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothchat.R
import com.example.bluetoothchat.adapter.item.DeviceItem

class DeviceRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {
    private val deviceItems by lazy { ArrayList<DeviceItem>() }
    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val deviceNameTextView: TextView = view.findViewById(R.id.device_name_text_view)
        val deviceAddressTextView: TextView = view.findViewById(R.id.device_address_text_view)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onItemClickListener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = deviceItems[position]
        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_remote_device_48))
        holder.deviceNameTextView.text = item.deviceName
        holder.deviceAddressTextView.text = item.deviceAddress
    }

    override fun getItemCount(): Int = deviceItems.size

    fun addItem(deviceName: String, deviceAddress: String) {
        val deviceItem = DeviceItem(deviceName, deviceAddress)
        deviceItems.add(deviceItem)
        notifyItemInserted(deviceItems.size-1)
    }

    fun getItem(position: Int) = deviceItems[position]

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}