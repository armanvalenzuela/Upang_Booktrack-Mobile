package com.arc_templars.upangbooktrack

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item

class ItemAdapter(
    private var itemList: List<Item>,
    private val onItemClick: (Item) -> Unit // ✅ Added Click Listener
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemDepartment: TextView = view.findViewById(R.id.itemDepartment)
        val itemCategory: TextView = view.findViewById(R.id.itemCategory)
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
        holder.itemCategory.text = item.category
        holder.itemDepartment.text = item.department

        // Set availability text and color
        if (item.availability) {
            holder.itemAvailability.text = "Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        } else {
            holder.itemAvailability.text = "Not Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }

        // ✅ Set Click Listener
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = itemList.size

    fun updateData(newList: List<Item>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
