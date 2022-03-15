package com.example.chatui.recyclerviews.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatui.R
import com.example.chatui.recyclerviews.items.DeviceItem

class DeviceRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {
    private val deviceItems by lazy { ArrayList<DeviceItem>() }
    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: ConstraintLayout = view.findViewById(R.id.device_recycler_view)
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val nameView: TextView = view.findViewById(R.id.name_text_view)
        val addressView: TextView = view.findViewById(R.id.address_text_view)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) onItemClickListener.onItemClick(view, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = deviceItems[position]
        viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_remote_device_48))
        viewHolder.nameView.text = item.name
        viewHolder.addressView.text = item.address
    }

    override fun getItemCount() = deviceItems.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun addItem(name: String?, address: String) {
        val item = DeviceItem(name, address)
        deviceItems.add(item)
        notifyItemInserted(deviceItems.size-1)
    }

    fun clear() {
        deviceItems.clear()
        notifyDataSetChanged()
    }
}