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
import org.json.JSONObject
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
        @Field("uniform_size") size: String,
        @Field("gender") gender: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user_request_book.php")
    fun requestBook(
        @Field("book_id") bookId: Int,
        @Field("user_id") userId: Int
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user_bookmark_item.php")
    fun bookmarkItem(
        @Field("user_id") userId: Int,
        @Field("book_id") bookId: Int?,
        @Field("uniform_id") uniformId: Int?
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user_check_bookmark.php") // Match your PHP filename
    fun checkBookmarkStatus(
        @Field("user_id") userId: Int,
        @Field("book_id") bookId: Int?,
        @Field("uniform_id") uniformId: Int?
    ): Call<ResponseBody>
}


class ItemDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val itemImageDetail: ImageView = findViewById(R.id.itemImage)
        val itemTitle: TextView = findViewById(R.id.itemTitle)
        val itemDescription: TextView = findViewById(R.id.itemDescription)
        val descriptionTextView = findViewById<TextView>(R.id.DescriptionforItems)
        val itemStocks: TextView = findViewById(R.id.itemStocks)
        val itemSizes: TextView = findViewById(R.id.itemSizes)
        val itemGender: TextView = findViewById(R.id.itemGender) // Gender TextView
        val itemAvailability: TextView = findViewById(R.id.itemAvailability)
        val backButton: ImageView = findViewById(R.id.btnBack)
        val btnRequest: Button = findViewById(R.id.btnRequest) // Request Button
        val btnSave: ImageView = findViewById(R.id.btnSave) // Get the ImageView

        val itemType = intent.getStringExtra("itemType") // "book" or "uniform"
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val stock = intent.getIntExtra("stock", -1)
        val sizes = intent.getStringExtra("sizes") ?: ""
        val gender = intent.getStringExtra("gender") ?: "Unspecified"
        val imageUrl = intent.getStringExtra("imageResId") ?: ""
        val uniformId = intent.getIntExtra("uniform_id", -1) // Get uniform ID
        val availability = intent.getBooleanExtra("availability", false) //Get availability from API
        val isAvailable = availability // Use API response directly
        val DescriptionforItems = intent.getStringExtra("item_description") ?: "No description available"
        findViewById<TextView>(R.id.DescriptionforItems).text = DescriptionforItems


        //SHAREDPREF TO GET USER VALUES
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        Log.d("ItemDetail", "Uniform ID: $uniformId, User ID: $userId")

        // Check bookmark status when activity starts
        if (itemType == "book") {
            val bookId = intent.getIntExtra("book_id", -1)
            if (bookId != -1 && userId != -1) {
                checkBookmarkStatus(userId, bookId, null)
            }
        } else if (itemType == "uniform") {
            if (uniformId != -1 && userId != -1) {
                checkBookmarkStatus(userId, null, uniformId)
            }
        }

        //IMAGE LOADING
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(itemImageDetail)

        // DYNAMIC IMAGE SIZE ADJUSTMENT FOR BOOKS
        if (itemType == "book") {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // Adjust height based on width to maintain aspect ratio (change ratio if needed)
            val imageHeight = (screenWidth * 1.3).toInt()

            itemImageDetail.layoutParams.width = screenWidth
            itemImageDetail.layoutParams.height = imageHeight
            itemImageDetail.scaleType = ImageView.ScaleType.FIT_CENTER
            itemImageDetail.requestLayout()
        }

        itemTitle.text = title

        //DESCRIPTION FORMATTING
        if (description != null) {
            itemDescription.text = HtmlCompat.fromHtml(
                "<b>${description.replace("|", "</b> | <b>")}</b>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        itemGender.text =
            HtmlCompat.fromHtml("<b>Gender:</b> $gender", HtmlCompat.FROM_HTML_MODE_LEGACY)

        //CONDITIONALS SO THAT IF ITEM IS BOOK, GENDER AND SIZE ISNT SHOWN
        if (itemType == "book") {
            itemStocks.text = "Stock: $stock"
            itemStocks.visibility = TextView.VISIBLE
            itemSizes.visibility = TextView.GONE
        } else if (itemType == "uniform") {
            itemSizes.text = formatSizes(sizes)
            itemSizes.visibility = TextView.VISIBLE
            itemStocks.visibility = TextView.GONE
        }

        backButton.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            if (itemType == "book") {
                sendBookmarkRequest(userId, intent.getIntExtra("book_id", -1), null)
            } else if (itemType == "uniform") {
                sendBookmarkRequest(userId, null, uniformId)
            } else {
                Toast.makeText(this, "Unknown item type!", Toast.LENGTH_SHORT).show()
            }
        }

        //CHECK AVAILABILITY HIGHLIGHT COLOR TEXT
        if (isAvailable) {
            itemAvailability.text = "Available"
            itemAvailability.setTextColor(resources.getColor(R.color.green, theme)) // Change to actual green color resource
        } else {
            itemAvailability.text = "Not Available"
            itemAvailability.setTextColor(resources.getColor(android.R.color.holo_red_dark)) // Change to actual red color resource
        }

        //CHECK AVAILABILITY, IF TRUE HIDE BUTTON IF FALSE SHOW
        if (isAvailable) {
            btnRequest.setBackgroundColor(resources.getColor(android.R.color.darker_gray)) // Set to gray
            btnRequest.isEnabled = true // Keep it clickable for toast
            btnRequest.setOnClickListener {
                Toast.makeText(this, "Item is available, cannot request", Toast.LENGTH_SHORT).show()
            }
        } else {
            btnRequest.isEnabled = true
            btnRequest.visibility = View.VISIBLE

            btnRequest.setOnClickListener {
                when (itemType) {
                    "book" -> {
                        val bookId = intent.getIntExtra("book_id", -1)
                        if (bookId != -1 && userId != -1) {
                            sendBookRequest(bookId, userId)
                        } else {
                            Toast.makeText(this, "Invalid book or user!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "uniform" -> {
                        if (uniformId != -1 && userId != -1) {
                            showSizeSelectionDialog(uniformId, userId, sizes, gender)
                        } else {
                            Toast.makeText(this, "Invalid uniform or user!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        Toast.makeText(this, "Unknown item type!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // FUNCT TO SEND UNIFORM REQUEST
    private fun sendUnifRequest(uniformId: Int, userId: Int, size: String, gender: String) {
        val apiService = ApiClient.getRetrofitInstance().create(RequestService::class.java)

        Log.d("sendUnifRequest", "Sending request - Uniform ID: $uniformId, User ID: $userId, Size: $size, Gender: $gender")
        apiService.requestUniform(uniformId, userId, size, gender).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonResponse = response.body()!!.string()
                        val jsonObject = JSONObject(jsonResponse)

                        // Get the message directly from JSON response
                        Toast.makeText(applicationContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "Response error!", Toast.LENGTH_SHORT).show()
                        Log.e("sendUnifRequest", "JSON Parsing error: ${e.message}")
                    }
                } else {
                    Toast.makeText(applicationContext, "Request failed! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "Network error! Check connection.", Toast.LENGTH_SHORT).show()
                Log.e("sendUnifRequest", "Request failed: ${t.message}")
            }
        })
    }

    private fun sendBookRequest(bookId: Int, userId: Int) {
        val apiService = ApiClient.getRetrofitInstance().create(RequestService::class.java)

        Log.d("sendBookRequest", "Sending request - Book ID: $bookId, User ID: $userId")

        apiService.requestBook(bookId, userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonResponse = response.body()!!.string()
                        val jsonObject = JSONObject(jsonResponse)

                        val message = jsonObject.getString("message")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "Response error!", Toast.LENGTH_SHORT).show()
                        Log.e("sendBookRequest", "JSON Parsing error: ${e.message}")
                    }
                } else {
                    Toast.makeText(applicationContext, "Request failed! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "Network error! Check connection.", Toast.LENGTH_SHORT).show()
                Log.e("sendBookRequest", "Request failed: ${t.message}")
            }
        })
    }

    //SIZE SELECTION FOR UNIFORM WHEN REQUESTING
    private fun showSizeSelectionDialog(uniformId: Int, userId: Int, sizes: String, gender: String) {
        val sizeOptions = sizes.split(" ")
            .filter { it.contains(":") }
            .map { it.split(":")[0] } // Extract size names only

        if (sizeOptions.isEmpty()) {
            Toast.makeText(this, "No available sizes!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_request, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background) // Apply rounded corners


        val btnRequest = dialogView.findViewById<Button>(R.id.btnRequest)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val sizeContainer = dialogView.findViewById<LinearLayout>(R.id.sizeContainer)

        var selectedSize = sizeOptions[0] // Default to first available size

        val radioGroup = RadioGroup(this)
        radioGroup.orientation = RadioGroup.VERTICAL // Ensure vertical layout

        sizeOptions.forEachIndexed { index, size ->
            val radioButton = RadioButton(this).apply {
                text = size
                id = View.generateViewId()
                setOnClickListener { selectedSize = size }
            }
            radioGroup.addView(radioButton)
            if (index == 0) {
                radioButton.isChecked = true
            }
        }
        sizeContainer.addView(radioGroup)


        btnRequest.setOnClickListener {
            sendUnifRequest(uniformId, userId, selectedSize, gender)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    //SIZE FORMATTING
    private fun formatSizes(sizes: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()

        //
        val label = SpannableString("Sizes:\n")
        label.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.append(label)

        if (sizes.isEmpty()) {
            spannable.append("Not Available")
            return spannable
        }

        val sizeList = sizes.split(" ").filter { it.contains(":") }

        sizeList.forEachIndexed { index, sizeEntry ->
            val (size, count) = sizeEntry.split(":")

            // Bold size type
            val sizeSpan = SpannableString("$size:")
            sizeSpan.setSpan(StyleSpan(Typeface.BOLD), 0, sizeSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append(sizeSpan)

            // Normal count (e.g., "55")
            spannable.append(" $count")

            if (index != sizeList.size - 1) {
                spannable.append(" | ") // Add separator except for last item
            }
        }
        return spannable
    }

    private fun sendBookmarkRequest(userId: Int, bookId: Int?, uniformId: Int?) {
        val apiService = ApiClient.getRetrofitInstance().create(RequestService::class.java)

        Log.d("sendBookmarkRequest", "Sending bookmark - User ID: $userId, Book ID: $bookId, Uniform ID: $uniformId")

        apiService.bookmarkItem(userId, bookId, uniformId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonResponse = response.body()!!.string()
                        val jsonObject = JSONObject(jsonResponse)

                        // Show message from server response
                        Toast.makeText(applicationContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()

                        // Refresh bookmark status after successful action
                        if (bookId != null) {
                            checkBookmarkStatus(userId, bookId, null)
                        } else if (uniformId != null) {
                            checkBookmarkStatus(userId, null, uniformId)
                        }

                    } catch (e: Exception) {
                        Log.e("sendBookmarkRequest", "JSON Parsing error: ${e.message}")
                        Toast.makeText(applicationContext, "Response error!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ServerResponse", "Error response: $errorBody")
                    Toast.makeText(applicationContext, "Bookmark failed! Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("sendBookmarkRequest", "Request failed: ${t.message}")
                Toast.makeText(applicationContext, "Network error! Check connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkBookmarkStatus(userId: Int, bookId: Int?, uniformId: Int?) {
        val apiService = ApiClient.getRetrofitInstance().create(RequestService::class.java)

        // Log the request being sent
        Log.d("BookmarkCheck", "Checking status - User: $userId, Book: $bookId, Uniform: $uniformId")

        apiService.checkBookmarkStatus(userId, bookId, uniformId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val jsonResponse = response.body()?.string()
                        // SINGLE COMPREHENSIVE LOG (what you asked for)
                        Log.d("BookmarkCheck", "PHP Response: $jsonResponse")

                        val jsonObject = JSONObject(jsonResponse)
                        val isBookmarked = jsonObject.getString("BMstatus") == "bookmarked"
                        updateBookmarkIcon(isBookmarked)

                    } catch (e: Exception) {
                        Log.e("BookmarkCheck", "JSON parsing error", e)
                    }
                } else {
                    Log.e("BookmarkCheck", "Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("BookmarkCheck", "Network error", t)
            }
        })
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        runOnUiThread {
            val btnSave = findViewById<ImageView>(R.id.btnSave)
            btnSave.setImageResource(
                if (isBookmarked) R.drawable.unsaved
                else R.drawable.save
            )
        }
    }

}