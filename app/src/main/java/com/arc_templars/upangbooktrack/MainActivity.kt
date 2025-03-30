package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface fetchAllApi {
    @GET("user_fetch_all.php")
    fun getAll(): Call<Map<String, List<Map<String, String>>>>

    @FormUrlEncoded
    @POST("user_check_notif.php")
    fun checkNotif(@Field("user_id") userId: Int): Call<Map<String, Boolean>>
}

data class BookUniformResponse(
    val books: List<BookData>,
    val uniforms: List<UniformData>
)

class MainActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var etSearchBar: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private var itemList = listOf<Item>()
    private lateinit var notificationBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationBadge = findViewById(R.id.notificationBadge)
        notificationBadge.visibility = View.GONE // Ensure it's hidden initially

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)
        Log.d("MainActivity", "Retrieved userId: $userId") // Add this log

        if (userId != -1) {
            checkUserNotifications(userId)
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshItems()

            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                checkUserNotifications(userId)
            }
        }

        // Show Dropdown Menu on Profile Icon Click
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener { showProfileMenu() }

        //NOTIFICATION CLICK LISTENER
        val notificationIcon = findViewById<View>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            val bottomSheet = UserNotifications()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        //Search Bar Implementation
        etSearchBar = findViewById(R.id.etsearchBar)
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())  // Calls the filtering function
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Bottom Navigation
        fetchAllItems()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_home
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_book -> {
                    val intent = Intent(this, Book::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_uniform -> {
                    val intent = Intent(this, Uniform::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_home -> true
                R.id.menu_bookmark -> {
                    val intent = Intent(this, Saved::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

    }

    private fun filterItems(query: String){
        val filteredList = itemList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        itemAdapter.updateData(filteredList)
    }

    private fun refreshItems() {
        itemList = emptyList()  // Clear current list
        itemAdapter.updateData(itemList) // Notify adapter
        fetchAllItems() // Fetch updated data
    }

    // FETCH ALL DATA (MEDYO MAGULO PERO TIIS TIIS INTINDIHIN NYO LANG)
    private fun fetchAllItems() {
        val apiService = ApiClient.getRetrofitInstance().create(fetchAllApi::class.java)

        // CALL THE API TO GET ALL DATA (BOTH BOOKS AND UNIFORMS)
        apiService.getAll().enqueue(object : Callback<Map<String, List<Map<String, String>>>> {
            override fun onResponse(
                call: Call<Map<String, List<Map<String, String>>>>,
                response: Response<Map<String, List<Map<String, String>>>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("MainActivity", "API Response: $data") // DEBUG LOG

                    if (data != null) {
                        val newList = mutableListOf<Item>() // CREATE A LIST TO STORE THE ITEMS

                        // DITO PRINO PROCESS BOOKS
                        data["books"]?.forEach { book ->
                            newList.add(
                                Item(
                                    book_id = book["book_id"]?.toIntOrNull(), // BOOK ID FROM STRING TO INT
                                    uniform_id = null, //IF ITS A BOOK NULL DAPAT SI UNIF ID
                                    name = book["bookname"] ?: "Unknown",
                                    imageResId = book["bookimage"] ?: "",
                                    availability = book["bookstat"] == "available",
                                    category = "Book",
                                    department = book["bookcollege"] ?: "",
                                    description = book["bookdesc"] ?: "",
                                    size = "",// EMPTY KASI WALA NAMANG SIZE ANG BOOKS
                                    gender = "", // EMPTY RIN KASI WALA NAMANG GENDER
                                    stock = book["bookstock"]?.toIntOrNull() ?: 0 //STOCK TO INT
                                )
                            )
                        }

                        // PROCESS ALL UNIFORMS FROM THE API RESPONSE
                        data["uniforms"]?.forEach { uniform ->
                            newList.add(
                                Item(
                                    book_id = null, // SINCE UNIFORM NO BOOK ID
                                    uniform_id = uniform["uniform_id"]?.toIntOrNull(), // CONVERT uniform_id FROM STRING TO INT
                                    name = uniform["uniformname"] ?: "Unknown",
                                    imageResId = uniform["uniformimage"] ?: "",
                                    availability = uniform["uniformstat"] == "available",
                                    category = "Uniform",
                                    department = uniform["uniformcollege"] ?: "",
                                    description = uniform["uniformdesc"] ?: "",
                                    size = uniform["uniformsize"] ?: "",
                                    gender = uniform["uniformgender"] ?: "",
                                    stock = uniform["uniformstock"]?.toIntOrNull() ?: 0 // CONVERT STOCK TO INT, DEFAULT TO 0 IF EMPTY
                                )
                            )
                        }

                        // UPDATE THE RECYCLERVIEW WITH THE NEW LIST OF ITEMS
                        itemList = newList
                        itemAdapter.updateData(itemList)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<Map<String, List<Map<String, String>>>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to Show item detail, fixed for both books and unif -kenchi
    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1) // Get user_id from session

        intent.putExtra("itemType", if (item.uniform_id == null) "book" else "uniform")
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department}")
        intent.putExtra("item_description", item.description)
        intent.putExtra("imageResId", item.imageResId)
        intent.putExtra("availability", item.availability)
        intent.putExtra("user_id", userId) // ✅ Pass user ID from session

        if (item.uniform_id == null) {
            // It's a book
            intent.putExtra("book_id", item.book_id) // ✅ Pass book ID
            intent.putExtra("stock", item.stock)
        } else {
            // It's a uniform
            val relatedSizes = itemList
                .filter { it.name == item.name && it.department == item.department && it.gender == item.gender }
                .joinToString(" ") { "${it.size}:${it.stock}" }

            intent.putExtra("uniform_id", item.uniform_id) // ✅ Pass uniform ID
            intent.putExtra("sizes", relatedSizes) // ✅ Pass related sizes
            intent.putExtra("gender", item.gender)
        }

        startActivity(intent)
    }



    // Function to Show Profile Dropdown Menu
    private fun showProfileMenu() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_profile_menu, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()

        // Apply transparent background
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // **Set custom width & height**
        alertDialog.setOnShowListener {
            val window = alertDialog.window
            window?.setLayout(400 * resources.displayMetrics.density.toInt(), 375 * resources.displayMetrics.density.toInt()) // Convert dp to pixels
        }

        // Find views
        val txtUserInfo = dialogView.findViewById<TextView>(R.id.txtUserInfo)
        val btnChangePassword = dialogView.findViewById<LinearLayout>(R.id.btnChangePassword)
        val btnLogout = dialogView.findViewById<LinearLayout>(R.id.btnLogout)

        // Load user details
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Lastname, Firstname") ?: "Lastname, Firstname"
        val studentNumber = sharedPreferences.getString("studentNumber", "N/A") ?: "N/A"

        txtUserInfo.text = "Name: $studentName"

        // Button click listeners
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePassActivity::class.java))
            alertDialog.dismiss()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Function to Show Log Out Confirmation Dialog
    private fun showLogoutConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Apply animation
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Find views
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirmLogout = dialogView.findViewById<Button>(R.id.btnConfirmLogout)

        // Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Confirm Logout button
        btnConfirmLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Completely remove all stored user data
            editor.clear()
            editor.apply()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears all previous activities
            startActivity(intent)
            finish() // Close MainActivity

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkUserNotifications(userId: Int) {
        Log.d("MainActivity", "Checking notifications for user: $userId")
        val apiService = ApiClient.getRetrofitInstance().create(fetchAllApi::class.java)

        apiService.checkNotif(userId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    Log.d("MainactTest", "Notification Response: $responseBody") // Debug log

                    val hasNotif = responseBody?.get("hasNotif") ?: false
                    notificationBadge.visibility = if (hasNotif) View.VISIBLE else View.GONE
                } else {
                    Log.e("MainActivity", "Failed to fetch notifications, response: ${response.errorBody()?.string()}")
                    notificationBadge.visibility = View.GONE // Ensure it's hidden on failure
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
                notificationBadge.visibility = View.GONE // Hide on error
            }
        })
    }

    private var backPressedTime: Long = 0

    override fun onBackPressed() {
        if (backPressedTime + 5000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity() // Closes the app
        } else {
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
