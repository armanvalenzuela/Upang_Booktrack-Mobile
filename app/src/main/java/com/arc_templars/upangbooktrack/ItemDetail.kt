package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface RequestService {
    @FormUrlEncoded
    @POST("user_request_unif.php")
    fun requestUniform(
        @Field("uniform_id") uniformId: Int,
        @Field("user_id") userId: Int,
        @Field("uniform_size") size: String
    ): Call<ResponseBody>
}

class ItemDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val itemImageDetail: ImageView = findViewById(R.id.itemImage)
        val itemTitle: TextView = findViewById(R.id.itemTitle)
        val itemDescription: TextView = findViewById(R.id.itemDescription)
        val itemStocks: TextView = findViewById(R.id.itemStocks)
        val itemSizes: TextView = findViewById(R.id.itemSizes)
        val itemGender: TextView = findViewById(R.id.itemGender) // ✅ Gender TextView
        val itemAvailability: TextView = findViewById(R.id.itemAvailability)
        val backButton: ImageView = findViewById(R.id.btnBack)
        val btnRequest: Button = findViewById(R.id.btnRequest) // ✅ Request Button

        val itemType = intent.getStringExtra("itemType") // "book" or "uniform"
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val stock = intent.getIntExtra("stock", -1)
        val sizes = intent.getStringExtra("sizes") ?: ""
        val gender = intent.getStringExtra("gender") ?: "Unspecified"
        val imageUrl = intent.getStringExtra("imageResId") ?: ""
        val uniformId = intent.getIntExtra("uniform_id", -1) // Get uniform ID

        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        Log.d("ItemDetail", "Uniform ID: $uniformId, User ID: $userId")

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

        // Handle request button click
        btnRequest.setOnClickListener {
            if (uniformId != -1 && userId != -1) {
                showSizeSelectionDialog(uniformId, userId, sizes)
            } else {
                Toast.makeText(this, "Invalid item or user!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // API call to request uniform
    private fun sendRequest(uniformId: Int, userId: Int, size: String) {
        val apiService = ApiClient.getRetrofitInstance().create(RequestService::class.java)

        Log.d("sendRequest", "Sending request - Uniform ID: $uniformId, User ID: $userId, Size:")
        apiService.requestUniform(uniformId, userId, size).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Request for size $size successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Request failed! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "Network error! Check connection.", Toast.LENGTH_SHORT).show()
                Log.e("ItemDetail", "Request failed: ${t.message}")
            }
        })
    }

    private fun showSizeSelectionDialog(uniformId: Int, userId: Int, sizes: String) {
        val sizeOptions = sizes.split(" ")
            .filter { it.contains(":") && !it.endsWith(":0") } // Exclude out-of-stock sizes
            .map { it.split(":")[0] } // Extract size names only

        if (sizeOptions.isEmpty()) {
            Toast.makeText(this, "No available sizes!", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Request What Size?")

        var selectedSize = sizeOptions[0] // Default selection
        builder.setSingleChoiceItems(sizeOptions.toTypedArray(), 0) { _, which ->
            selectedSize = sizeOptions[which]
        }

        builder.setPositiveButton("Request") { _, _ ->
            sendRequest(uniformId, userId, selectedSize)
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
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
