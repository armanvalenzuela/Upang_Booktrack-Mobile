//TODO: add bookmark functionality

package com.arc_templars.upangbooktrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arc_templars.upangbooktrack.models.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface fetchSavedApi {
    @FormUrlEncoded
    @POST("user_fetch_bookmarks.php")
    fun getBookmarked(@Field("user_id") userId: Int): Call<Map<String, Any>>

    @FormUrlEncoded
    @POST("user_check_notif.php")
    fun checkNotif(@Field("user_id") userId: Int): Call<Map<String, Boolean>>
}


class Saved : AppCompatActivity() {

    private lateinit var notificationBadge: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFilter: ImageView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var etSearchBar: EditText
    private var selectedCategory: String? = null
    private var selectedAvailability: String = "All"
    private var itemList = listOf<Item>()

    override fun onResume() {
        super.onResume()
        fetchSavedItems()

        // Also refresh notifications when coming back to activity
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)
        if (userId != -1) {
            checkUserNotifications(userId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        // DROPDOWN MENU FOR PROFILE
        profileIcon.setOnClickListener { showProfileMenu() }

        //NOTIFICATION CLICK LISTENER
        val notificationIcon = findViewById<View>(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            val bottomSheet = UserNotifications()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        notificationBadge = findViewById(R.id.notificationBadge)
        notificationBadge.visibility = View.GONE

        // Check notifications
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)
        if (userId != -1) {
            checkUserNotifications(userId)
        }

        recyclerView = findViewById(R.id.recyclerView)
        btnFilter = findViewById(R.id.btnFilter)

        // RECYCLERVIEW SETUP
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        itemAdapter = ItemAdapter(itemList) { item -> openItemDetail(item) }
        recyclerView.adapter = itemAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, true))

        // FILTER DIALOG ON CLICK
        btnFilter.setOnClickListener { showFilterDialog() }

        //SEARCH BAR FUNCTION
        etSearchBar = findViewById(R.id.etsearchBar)
        etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterItems(s.toString())  // Calls the filtering function
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)

        // SWIPE REFRESH LAYOUT FUNCTION
        swipeRefreshLayout.setOnRefreshListener {
            itemList = emptyList()
            itemAdapter.updateData(itemList)
            fetchSavedItems()

            // Also check notifications on refresh
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("id", -1)
            if (userId != -1) {
                checkUserNotifications(userId)
            }
        }

        fetchSavedItems()

        // BOTTOM NAV
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_bookmark
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
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
                R.id.menu_bookmark -> true
                else -> false
            }
        }
    }

    //FETCH USER BOOKMARKED ITEMS
    private fun fetchSavedItems() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.getRetrofitInstance().create(fetchSavedApi::class.java)

        apiService.getBookmarked(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data["success"] == true) {
                        val newList = mutableListOf<Item>()

                        // SETS DATA FOR BOOKS
                        (data["books"] as? List<Map<String, Any>>)?.forEach { book ->
                            newList.add(
                                Item(
                                    book_id = parseNumberToInt(book["book_id"]),
                                    uniform_id = null,
                                    name = book["bookname"] as? String ?: "Unknown",
                                    imageResId = book["bookimage"] as? String ?: "",
                                    availability = (book["bookstat"] as? String) == "available",
                                    category = "Book",
                                    department = book["bookcollege"] as? String ?: "",
                                    description = book["bookdesc"] as? String ?: "",
                                    size = "",
                                    gender = "",
                                    stock = parseNumberToInt(book["bookstock"]) ?: 0
                                )
                            )
                        }

                        // SETS DATA FOR UNIFORMS
                        (data["uniforms"] as? List<Map<String, Any>>)?.forEach { uniform ->
                            newList.add(
                                Item(
                                    book_id = null,
                                    uniform_id = parseNumberToInt(uniform["uniform_id"]),
                                    name = uniform["uniformname"] as? String ?: "Unknown",
                                    imageResId = uniform["uniformimage"] as? String ?: "",
                                    availability = (uniform["uniformstat"] as? String) == "available",
                                    category = "Uniform",
                                    department = uniform["uniformcollege"] as? String ?: "",
                                    description = uniform["uniformdesc"] as? String ?: "",
                                    size = uniform["uniformsize"] as? String ?: "",
                                    gender = uniform["uniformgender"] as? String ?: "",
                                    stock = parseNumberToInt(uniform["uniformstock"]) ?: 0
                                )
                            )
                        }

                        itemList = newList
                        itemAdapter.updateData(itemList)

                        // LOGGING
                        Log.d("SavedActivity", "=== Saved Items (${itemList.size}) ===")
                        itemList.forEachIndexed { index, item ->
                            Log.d("SavedActivity", """
                            [Item ${index + 1}]
                            Type: ${if (item.book_id != null) "Book (ID:${item.book_id})" else "Uniform (ID:${item.uniform_id})"}
                            Name: ${item.name}
                            Category: ${item.category}
                            Available: ${item.availability}
                            Department: ${item.department}
                            Stock: ${item.stock}
                            Image: ${item.imageResId.take(30)}...
                        """.trimIndent())
                        }
                    } else {
                        Toast.makeText(this@Saved, "No bookmarked items found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Saved, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@Saved, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    // PARSE ID NO TO INT !!!IMPORTANT!!!
    private fun parseNumberToInt(number: Any?): Int? {
        return when (number) {
            is Double -> number.toInt()
            is Int -> number
            is String -> number.toIntOrNull()
            else -> null
        }
    }


    //FILTER FUNCTION
    private fun filterItems(query: String){
        val filteredList = itemList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        itemAdapter.updateData(filteredList)
    }

    // PASS DATA TO ITEM DETAIL
    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetail::class.java)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1) // Get user_id from session

        intent.putExtra("itemType", if (item.uniform_id == null) "book" else "uniform")
        Log.d("ItemCheck", "Item Data: $item")
        intent.putExtra("title", item.name)
        intent.putExtra("description", "${item.category} | ${item.department}")
        intent.putExtra("item_description", item.description)
        intent.putExtra("imageResId", item.imageResId)
        intent.putExtra("availability", item.availability)
        intent.putExtra("user_id", userId) // ✅ Pass user ID from session

        if (item.uniform_id == null) {
            // It's a book
            intent.putExtra("book_id", item.book_id) // ✅ Pass book ID
            Log.d("ItemDetail", "Sending Book ID: ${item.book_id}")
            intent.putExtra("stock", item.stock)
        } else {
            // It's a uniform
            val relatedSizes = itemList
                .filter { it.name == item.name && it.department == item.department && it.gender == item.gender }
                .joinToString(" ") { "${it.size}:${it.stock}" }

            intent.putExtra("uniform_id", item.uniform_id) // ✅ Pass uniform ID
            Log.d("ItemDetail", "Sending Uniform ID: ${item.uniform_id}")
            intent.putExtra("sizes", relatedSizes) // ✅ Pass related sizes
            intent.putExtra("gender", item.gender)
        }

        // LOGGING
        Log.d("ItemDetailIntent", """
        itemType: ${intent.getStringExtra("itemType")}
        title: ${intent.getStringExtra("title")}
        description: ${intent.getStringExtra("description")}
        item_description: ${intent.getStringExtra("item_description")}
        imageResId: ${intent.getStringExtra("imageResId")}
        availability: ${intent.getBooleanExtra("availability", false)}
        user_id: ${intent.getStringExtra("user_id")}
        ${if (item.uniform_id == null)
            "book_id: ${intent.getIntExtra("book_id", -1)}\nstock: ${intent.getIntExtra("stock", -1)}"
        else
            "uniform_id: ${intent.getIntExtra("uniform_id", -1)}\nsizes: ${intent.getStringExtra("sizes")}\ngender: ${intent.getStringExtra("gender")}"}
    """.trimIndent())

        startActivity(intent)
    }


    // FILTER FUNCTION
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.filter, null)

        val departmentGroup = view.findViewById<RadioGroup>(R.id.departmentGroup)
        val availabilitySpinner = view.findViewById<Spinner>(R.id.availabilitySpinner)
        val applyButton = view.findViewById<Button>(R.id.applyFilterButton)

        // Restore Previous Department Selection
        when (selectedCategory) {
            "CEA" -> departmentGroup.check(R.id.department_cea)
            "CMA" -> departmentGroup.check(R.id.department_cma)
            "CAHS" -> departmentGroup.check(R.id.department_cahs)
            "CCJE" -> departmentGroup.check(R.id.department_ccje)
            "CITE" -> departmentGroup.check(R.id.department_cite)
            "SHS" -> departmentGroup.check(R.id.department_shs)
            "CAS" -> departmentGroup.check(R.id.department_cas)
        }

        // AVAILABILITY SELECTION
        val availabilityOptions = arrayOf("All", "Available", "Not Available")

        // Set custom layout for Spinner
        val adapter = ArrayAdapter(view.context, R.layout.spinner_dropdown_item, availabilityOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        availabilitySpinner.adapter = adapter
        availabilitySpinner.setSelection(availabilityOptions.indexOf(selectedAvailability))

        // SET SELECTED DEPARTMENT
        departmentGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.department_cea -> "CEA"
                R.id.department_cma -> "CMA"
                R.id.department_cahs -> "CAHS"
                R.id.department_ccje -> "CCJE"
                R.id.department_cite -> "CITE"
                R.id.department_shs -> "SHS"
                R.id.department_cas -> "CAS"
                else -> null
            }
        }

        // SET AVAILABILITY
        availabilitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAvailability = availabilityOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // APPLY FILTERS AND CLOSE
        applyButton.setOnClickListener {
            applyFilters()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // APPLY FILTERS
    private fun applyFilters() {
        var filteredList = itemList

        // Filter by Department
        if (selectedCategory != null) {
            filteredList = filteredList.filter { it.department == selectedCategory }
        }

        // FILTER BY AVAILABILITY
        when (selectedAvailability) {
            "Available" -> filteredList = filteredList.filter { it.availability }
            "Not Available" -> filteredList = filteredList.filter { !it.availability }
        }

        // UPDATE
        itemAdapter.updateData(filteredList)
    }

    // PROFILE MENU DROPDOWN
    private fun showProfileMenu() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_profile_menu, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()

        // TRANSPARENT BG
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // CUSTOM ALERTDIALOG SIZE
        alertDialog.setOnShowListener {
            val window = alertDialog.window
            window?.setLayout(400 * resources.displayMetrics.density.toInt(), 375 * resources.displayMetrics.density.toInt()) // Convert dp to pixels
        }

        // STUDENT INFO
        val txtUserInfo = dialogView.findViewById<TextView>(R.id.txtUserInfo)
        val txtStudentNo = dialogView.findViewById<TextView>(R.id.txtStudentNo)
        val btnChangePassword = dialogView.findViewById<LinearLayout>(R.id.btnChangePassword)
        val btnLogout = dialogView.findViewById<LinearLayout>(R.id.btnLogout)

        // SHAREDPREF LOAD FOR USER DETAILS
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = sharedPreferences.getString("studentName", "Lastname, Firstname") ?: "Lastname, Firstname"
        val studentNo = sharedPreferences.getString("studentNo", "N/A") ?: "N/A"

        txtUserInfo.text = "Name: $studentName"
        txtStudentNo.text = "Student No: $studentNo"

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

    // LOGOUT CONFIRMATION DIALOG
    private fun showLogoutConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirmation, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // ANIMATION
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // VIEW INIT
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirmLogout = dialogView.findViewById<Button>(R.id.btnConfirmLogout)

        // CANCEL
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // LOGOUT CONFIRMATION
        btnConfirmLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // REMOVE ALL DATA
            editor.clear()
            editor.apply()

            // BACK TO LOGIN
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears all previous activities
            startActivity(intent)
            finish() // Close MainActivity

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkUserNotifications(userId: Int) {
        Log.d("SavedActivity", "Checking notifications for user: $userId")
        val apiService = ApiClient.getRetrofitInstance().create(fetchSavedApi::class.java)

        apiService.checkNotif(userId).enqueue(object : Callback<Map<String, Boolean>> {
            override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()
                    Log.d("SavedActivity", "Notification Response: $responseBody")
                    val hasNotif = responseBody?.get("hasNotif") ?: false
                    notificationBadge.visibility = if (hasNotif) View.VISIBLE else View.GONE
                } else {
                    Log.e("SavedActivity", "Failed to fetch notifications")
                    notificationBadge.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                Log.e("SavedActivity", "API call failed: ${t.message}")
                notificationBadge.visibility = View.GONE
            }
        })
    }

    // SET INITIAL TIME
    private var backPressedTime: Long = 0

    //ON BACKPRESS WITHIN 5 SECS AFTER INITIAL BACKPRESS WILL FINISH ACT
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
