package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
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
        // ✅ **Make Description Bold**
        if (description != null) {
            itemDescription.text = HtmlCompat.fromHtml(
                "<b>${description.replace("|", "</b> | <b>")}</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        itemGender.text = HtmlCompat.fromHtml("<b>Gender:</b> $gender", HtmlCompat.FROM_HTML_MODE_LEGACY)

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

        val dialogView = layoutInflater.inflate(R.layout.dialog_request, null)
        val btnRequest = dialogView.findViewById<Button>(R.id.btnRequest)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val sizeContainer = dialogView.findViewById<LinearLayout>(R.id.sizeContainer)

        var selectedSize = sizeOptions[0] // ✅ Default to first available size

        // Create radio buttons dynamically
        val radioGroup = RadioGroup(this)
        sizeOptions.forEachIndexed { index, size ->
            val radioButton = RadioButton(this).apply {
                text = size
                id = View.generateViewId()
            }
            radioGroup.addView(radioButton)

            if (index == 0) {
                radioButton.isChecked = true // ✅ First size is selected by default
            }
        }
        sizeContainer.addView(radioGroup)

        // ✅ Create the dialog and apply custom background
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background) // ✅ Apply Custom Border

        btnRequest.setOnClickListener {
            sendRequest(uniformId, userId, selectedSize)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Size formatting
    private fun formatSizes(sizes: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()

        // Bold "Sizes:"
        val label = SpannableString("Sizes:\n")
        label.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.append(label)

        if (sizes.isEmpty()) {
            spannable.append("Not Available")
            return spannable
        }

        sizes.split(" ").filter { it.contains(":") }.forEach { sizeEntry ->
            val (size, count) = sizeEntry.split(":")

            // Bold size type (e.g., "M:")
            val sizeSpan = SpannableString("$size: ")
            sizeSpan.setSpan(StyleSpan(Typeface.BOLD), 0, sizeSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append(sizeSpan)

            // Append count (e.g., "15 pcs")
            spannable.append("$count \n")
        }

        return spannable
    }
}