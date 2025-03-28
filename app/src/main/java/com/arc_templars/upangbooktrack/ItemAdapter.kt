package com.arc_templars.upangbooktrack

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.bumptech.glide.Glide

class ItemAdapter(
    private var itemList: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var filteredList: MutableList<Item> = itemList.toMutableList()

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemSize: TextView = view.findViewById(R.id.itemSize)
        val itemGender: TextView = view.findViewById(R.id.itemGender)
        val itemDepartment: TextView = view.findViewById(R.id.itemDepartment)
        val itemCategory: TextView = view.findViewById(R.id.itemCategory)
        val itemAvailability: TextView = view.findViewById(R.id.itemAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = filteredList[position] // 🔹 Use filteredList instead of itemList

        Log.d("ItemAdapter", "Image URL: ${item.imageResId}")  // Debugging

        // 🔹 Ensure proper image loading (prevents issues with empty URLs)
        val imageUrl = item.imageResId.ifEmpty { "default_image_url" } // Replace with an actual default URL if needed

        Glide.with(holder.itemView.context)
            .load(imageUrl) // Load image safely
            .placeholder(R.drawable.placeholder) // Placeholder if image fails to load
            .into(holder.itemImage)

        holder.itemName.text = item.name

        // 🔹 Hide textview if value is null or empty (for books with no size or gender)
        if (item.size.isNullOrEmpty()) {
            holder.itemSize.visibility = View.GONE
        } else {
            holder.itemSize.visibility = View.VISIBLE
            holder.itemSize.text = "Size: ${item.size}"
        }

        if (item.gender.isNullOrEmpty()){
            holder.itemGender.visibility = View.GONE
        } else {
            holder.itemGender.visibility = View.VISIBLE
            holder.itemGender.text = "Gender: ${item.gender}"
        }

        holder.itemCategory.text = item.category
        holder.itemDepartment.text = item.department

        // 🔹 Set availability text and color
        if (item.availability) {
            holder.itemAvailability.text = "Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        } else {
            holder.itemAvailability.text = "Not Available"
            holder.itemAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = filteredList.size // 🔹 Use filteredList instead of itemList

    // 🔹 Filter Function for Search Bar
    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase().trim()
        filteredList = if (lowerCaseQuery.isEmpty()) {
            itemList.toMutableList() // Show all items when search is empty
        } else {
            itemList.filter {
                it.name.lowercase().contains(lowerCaseQuery) ||  // Search by Name
                        it.category.lowercase().contains(lowerCaseQuery) || // Search by Category
                        it.department.lowercase().contains(lowerCaseQuery) // Search by Department
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    // 🔹 Update Adapter Data
    fun updateData(newList: List<Item>) {
        itemList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}