package com.arc_templars.upangbooktrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item

class ItemAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemAvailability: TextView = view.findViewById(R.id.itemAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemImage.setImageResource(item.imageResId)
        holder.itemName.text = item.name

        // Set availability text and color
        if (item.availability) {
            holder.itemAvailability.text = "Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        } else {
            holder.itemAvailability.text = "Not Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }
    }

    override fun getItemCount() = itemList.size
}
