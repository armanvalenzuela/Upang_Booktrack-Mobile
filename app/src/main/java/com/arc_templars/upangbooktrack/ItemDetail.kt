package com.arc_templars.upangbooktrack

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ItemDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val itemImageDetail: ImageView = findViewById(R.id.itemImage)
        val itemTitle: TextView = findViewById(R.id.itemTitle)
        val itemDescription: TextView = findViewById(R.id.itemDescription)
        val itemStocks: TextView = findViewById(R.id.itemStocks)
        val itemSizes: TextView = findViewById(R.id.itemSizes)
        val itemGender: TextView = findViewById(R.id.itemGender) // ✅ Added Gender TextView
        val itemAvailability: TextView = findViewById(R.id.itemAvailability)
        val backButton: ImageView = findViewById(R.id.btnBack)

        val itemType = intent.getStringExtra("itemType") // "book" or "uniform"
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val stock = intent.getIntExtra("stock", -1)  // Default -1 if not a book
        val sizes = intent.getStringExtra("sizes") ?: ""
        val gender = intent.getStringExtra("gender") ?: "Unspecified"
        val imageResId = intent.getIntExtra("imageResId", R.drawable.placeholder)
        Log.d("ItemDetail", "imageResId: $imageResId")


        //fixed image displaying -kenchi
        val imageUrl = intent.getStringExtra("imageResId") ?: ""
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(itemImageDetail)

        itemTitle.text = title
        itemDescription.text = description
        itemGender.text = "Gender: $gender"

        if (itemType == "book") {
            itemStocks.text = "Stock: $stock"
            itemStocks.visibility = TextView.VISIBLE
            itemSizes.visibility = TextView.GONE
        } else if (itemType == "uniform") {
            itemSizes.text = formatSizes(sizes)
            itemSizes.visibility = TextView.VISIBLE
            itemStocks.visibility = TextView.GONE
        }

        val isAvailable = if (itemType == "book") stock > 0 else !sizes.contains("L: 0")
        itemAvailability.text = if (isAvailable) "Available" else "Not Available"
        itemAvailability.setTextColor(resources.getColor(if (isAvailable) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

        backButton.setOnClickListener { finish() }
    }

    // Size formatting
    private fun formatSizes(sizes: String): String {
        if (sizes.isEmpty()) return "Sizes: Not Available"

        val sizeList = sizes.split(" ")
            .filter { it.contains(":") }
            .joinToString("\n") { sizeEntry ->
                val (size, count) = sizeEntry.split(":")
                "$size: $count pcs"
            }

        return "Available Sizes:\n$sizeList"
    }
}
